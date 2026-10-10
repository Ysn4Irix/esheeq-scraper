require('dotenv').config()
const redis = require('redis')
const logger = require('../helpers/logger')
const { REDIS_HOST, REDIS_PORT, REDIS_PASSWORD } = process.env

/**
 * In-memory fallback cache with TTL and LRU-like eviction
 */
class InMemoryCache {
	constructor(maxEntries = 1000) {
		this.cache = new Map()
		this.maxEntries = maxEntries
	}

	async get(key) {
		const entry = this.cache.get(key)
		if (!entry) return null
		if (Date.now() > entry.expiresAt) {
			this.cache.delete(key)
			return null
		}
		return entry.value
	}

	async setEx(key, ttlSeconds, value) {
		if (this.cache.size >= this.maxEntries) {
			const now = Date.now()
			let evicted = false
			for (const [k, v] of this.cache.entries()) {
				if (now > v.expiresAt) {
					this.cache.delete(k)
					evicted = true
					break
				}
			}
			if (!evicted) {
				const firstKey = this.cache.keys().next().value
				if (firstKey) this.cache.delete(firstKey)
			}
		}
		this.cache.set(key, {
			value: typeof value === 'string' ? value : JSON.stringify(value),
			expiresAt: Date.now() + ttlSeconds * 1000
		})
		return 'OK'
	}

	async exists(key) {
		const val = await this.get(key)
		return val !== null
	}

	async quit() {
		this.cache.clear()
		return 'OK'
	}
}

const memoryFallback = new InMemoryCache()
let redisClient = null
let isRedisReady = false

if (REDIS_HOST) {
	try {
		redisClient = redis.createClient({
			socket: {
				host: REDIS_HOST,
				port: REDIS_PORT ? parseInt(REDIS_PORT, 10) : 6379,
				reconnectStrategy: retries => {
					if (retries > 5) {
						logger.warn(
							'Redis reconnection limit reached; operating in memory-cache fallback mode.'
						)
						return false
					}
					return Math.min(retries * 500, 2000)
				}
			},
			password: REDIS_PASSWORD || undefined
		})

		redisClient.on('ready', () => {
			isRedisReady = true
			logger.success('Client connected to redis and ready to use...')
		})

		redisClient.on('error', err => {
			isRedisReady = false
			logger.error(`Redis error: ${err.message}. Using in-memory fallback.`)
		})

		redisClient.on('end', () => {
			isRedisReady = false
			logger.info('Client disconnected from redis.')
		})

		redisClient.connect().catch(err => {
			isRedisReady = false
			logger.warn(
				`Could not connect to Redis at ${REDIS_HOST}:${REDIS_PORT || 6379}: ${
					err.message
				}. Using in-memory fallback.`
			)
		})
	} catch (err) {
		logger.warn(`Failed initializing Redis client: ${err.message}. Using in-memory fallback.`)
	}
} else {
	logger.info('REDIS_HOST not configured. Operating with in-memory cache fallback.')
}

// Proxy interface matching Redis operations
const clientProxy = {
	async get(key) {
		if (isRedisReady && redisClient) {
			try {
				return await redisClient.get(key)
			} catch (err) {
				logger.error(`Redis get error for ${key}: ${err.message}`)
			}
		}
		return await memoryFallback.get(key)
	},

	async setEx(key, ttlSeconds, value) {
		if (isRedisReady && redisClient) {
			try {
				return await redisClient.setEx(key, ttlSeconds, value)
			} catch (err) {
				logger.error(`Redis setEx error for ${key}: ${err.message}`)
			}
		}
		return await memoryFallback.setEx(key, ttlSeconds, value)
	},

	async exists(key) {
		if (isRedisReady && redisClient) {
			try {
				const res = await redisClient.exists(key)
				return res === 1 || res === true
			} catch (err) {
				logger.error(`Redis exists error for ${key}: ${err.message}`)
			}
		}
		return await memoryFallback.exists(key)
	},

	async quit() {
		if (redisClient) {
			try {
				await redisClient.quit()
			} catch (err) {
				// Suppress shutdown disconnect error
			}
		}
		await memoryFallback.quit()
	}
}

module.exports = clientProxy
