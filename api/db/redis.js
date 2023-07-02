require('dotenv').config()
const redis = require('redis')
const logger = require('../helpers/logger')
const { REDIS_HOST, REDIS_PORT, REDIS_PASSWORD } = process.env

const client = redis.createClient({
	socket: {
		host: REDIS_HOST,
		port: REDIS_PORT
	}
	//password: REDIS_PASSWORD
})

client.connect().then(() => logger.success('Client connected to redis...'))

client.on('ready', () => {
	logger.info('Client connected to redis and ready to use...')
})

client.on('error', err => {
	logger.error(err.message)
})

client.on('end', () => {
	logger.info('Client disconnected from redis')
})

module.exports = client
