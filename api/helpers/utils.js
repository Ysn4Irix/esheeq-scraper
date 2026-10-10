const axios = require('axios')
const cheerio = require('cheerio')
const base64 = require('base-64')
const logger = require('./logger')
const redisClient = require('../db/redis')
const { base_url } = require('../helpers/constants')

/**
 *
 * @param {string} url url of the series page to be scrapped
 * @returns {Promise<string>} html of the url
 */
const fetchHtml = async url => {
	const htmlDataKey = `html:${url.replace('https://', '')}`
	let htmlData
	if (await redisClient.exists(htmlDataKey)) {
		logger.info(`Cache hit for ${htmlDataKey}`)
		htmlData = await redisClient.get(htmlDataKey)
	} else {
		logger.info(`Cache miss for ${htmlDataKey}`)
		try {
			const response = await axios.get(url)
			htmlData = response.data
		} catch (error) {
			logger.error(`Error fetching ${url}: ${error}`)
			return null
		}
	}
	redisClient.setEx(htmlDataKey, 86400, htmlData)
	return htmlData
}

/**
 *
 * @param {string} url url of the series page to be scrapped
 * @returns {Promise<string>} html of the url
 */
const fetchEpisodesHtml = async (seriesId, url) => {
	const EpisodesHtmlDataKey = `html:${seriesId}:${url.replace(
		'https://',
		''
	)}`
	let htmlData
	if (await redisClient.exists(EpisodesHtmlDataKey)) {
		logger.info(`Cache hit for ${EpisodesHtmlDataKey}`)
		htmlData = await redisClient.get(EpisodesHtmlDataKey)
	} else {
		logger.info(`Cache miss for ${EpisodesHtmlDataKey}`)
		try {
			const response = await axios.get(url)
			htmlData = response.data
		} catch (error) {
			logger.error(`Error fetching ${url}: ${error}`)
			return null
		}
	}
	redisClient.setEx(EpisodesHtmlDataKey, 86400, htmlData)
	return htmlData
}

/**
 *
 * @param {string} seriesId series id
 * @param {array<string>} episodesUrls array of episodes urls
 * @param {number} batchSize batch concurrency size
 * @returns {Promise<array>} array of daily links
 */
const fetchDailyLinks = async (seriesId, episodesUrls, batchSize = 5) => {
	const dailyLinksKey = `dailyLinks:${seriesId}`

	let dailyLinks = []
	if (await redisClient.exists(dailyLinksKey)) {
		logger.info(`Cache hit for ${dailyLinksKey}`)
		try {
			dailyLinks = JSON.parse(await redisClient.get(dailyLinksKey))
			return dailyLinks
		} catch (err) {
			logger.error(`Error parsing cached dailyLinks: ${err.message}`)
		}
	}

	logger.info(
		`Cache miss for ${dailyLinksKey} - fetching ${episodesUrls.length} episodes with batch size ${batchSize}`
	)
	let hasFailures = false

	for (let i = 0; i < episodesUrls.length; i += batchSize) {
		const chunk = episodesUrls.slice(i, i + batchSize)
		const chunkResults = await Promise.all(
			chunk.map(async (url, idx) => {
				const episodeNum = `${i + idx + 1}`
				try {
					const html = await fetchEpisodesHtml(seriesId, url)
					if (html) {
						const dailyLink = extractDailymotionLinks(html)
						if (dailyLink) {
							return { episode: episodeNum, link: dailyLink }
						}
					}
				} catch (err) {
					logger.error(`Error resolving episode ${episodeNum}: ${err.message}`)
				}
				hasFailures = true
				return { episode: episodeNum, link: null, retryable: true }
			})
		)
		dailyLinks.push(...chunkResults)
	}

	// 5-minute TTL on partial failures, 24-hour TTL on complete success
	const ttl = hasFailures ? 300 : 86400
	await redisClient.setEx(dailyLinksKey, ttl, JSON.stringify(dailyLinks))
	return dailyLinks
}

/**
 *
 * @param {string} html html of the search result page
 * @returns {array<object>} array of search results
 */
const search = async query => {
	const searchHtmlKey = `searchHtml:${query}`
	let searchHtml
	if (await redisClient.exists(searchHtmlKey)) {
		logger.info(`Cache hit for ${searchHtmlKey}`)
		searchHtml = await redisClient.get(searchHtmlKey)
	} else {
		logger.info(`Cache miss for ${searchHtmlKey}`)
		searchHtml = await fetchHtml(`${base_url}/search/${query}`)
		redisClient.setEx(searchHtmlKey, 86400, searchHtml)
	}

	const $ = cheerio.load(searchHtml)

	const element = $('body > div.sec-line > div > div > article')

	let results = []
	element.each((i, el) => {
		const title = $(el).find('div.title').text().trim()
		if (!title.includes('فيلم')) {
			const href = $(el).find('a').attr('href')
			const url = href ? decodeURI(href) : ''
			const id = url ? url.split('/')[4] || '' : ''
			const imgStyle = $(el).find('div.imgBg').attr('style') || ''
			const cover =
				imgStyle.includes('(') && imgStyle.includes(')')
					? imgStyle.split('(')[1].split(')')[0].replace(/['"]/g, '')
					: ''
			results.push({
				id,
				title,
				cover,
				url
			})
		}
	})
	return results
}

/**
 *
 * @param {string} html html of the series page
 * @returns {object} object of series infos
 */
const extractSeriesInfos = html => {
	const $ = cheerio.load(html)

	const infoElement = $('body > div.singleSeries > div.info')
	const coverElement = $('body > div.singleSeries > div.cover')

	let infos = {
		id: '',
		title: '',
		cover: '',
		description: '',
		actors: []
	}

	infos.title = infoElement.find('h1').text().trim()
	infos.id = infos.title.replace(/ /g, '-')
	const coverStyle = coverElement.find('div.img').attr('style') || ''
	infos.cover =
		coverStyle.includes('(') && coverStyle.includes(')')
			? coverStyle.split('(')[1].split(')')[0].replace(/['"]/g, '')
			: ''
	infos.description = infoElement.find('div.story').text().trim()
	infoElement.find('div.tax > a').each((i, el) => {
		const actor = $(el).attr('title')
		if (actor) {
			infos.actors.push(actor)
		}
	})

	return infos
}

/**
 * @desc extract episodes urls from the series pages
 * @param {string} html html of the series page to be scrapped
 * @returns {array<string>} array of episodes urls
 */
const extractEpisodesUrls = html => {
	const $ = cheerio.load(html)

	const element = $('body > div.sec-line > div > div > article')

	let urls = []
	element.each((i, el) => {
		const link = $(el).find('a').attr('href')

		urls.push(convertUrl(link))
	})
	return urls.reverse()
}

/**
 * @desc extract dailymotion and direct streaming links from the episodes pages
 * @param {string} html
 * @returns {string|null} streaming link
 */
const extractDailymotionLinks = html => {
	try {
		const $ = cheerio.load(html)

		// Direct iframe check
		const iframeSrc = $(
			'iframe[src*="dailymotion.com"], iframe[src*="ok.ru"]'
		).attr('src')
		if (iframeSrc) return iframeSrc

		const element = $(
			'a[href*="watch?post="], body > div.secContainer.bg > div > div > div.getEmbed > div > a, .modern-player-container a'
		)
		const url = element.attr('href')
		if (!url) return null

		let jsonData = ''
		if (url.includes('post=')) {
			jsonData = url.split('post=')[1].split('&')[0]
		} else if (url.includes('=')) {
			const parts = url.split('=')
			jsonData = parts[parts.length - 1]
		}
		if (!jsonData) return null

		jsonData = decodeURIComponent(jsonData).replace(/%3D/g, '')
		const decodedData = Buffer.from(jsonData, 'base64').toString('utf-8')
		const data = JSON.parse(decodedData)

		if (!data) return null

		// Note: data.codeDaily is a static placeholder ("x6mp5r") in the upstream WordPress theme.
		// Never resolve codeDaily if it equals "x6mp5r".
		if (data.codeDaily && data.codeDaily !== 'x6mp5r') {
			return data.codeDaily.startsWith('http')
				? data.codeDaily
				: `https://www.dailymotion.com/video/${data.codeDaily}`
		}

		if (Array.isArray(data.servers)) {
			// 1. Prioritize Dailymotion server
			const dailyServer = data.servers.find(
				s => s && s.name && s.name.toLowerCase().includes('dailymotion')
			)
			if (dailyServer && dailyServer.id) {
				const id = dailyServer.id.trim()
				return id.startsWith('http')
					? id
					: `https://www.dailymotion.com/video/${id}`
			}

			// 2. Direct URLs (e.g. mail.ru express link)
			const directServer = data.servers.find(
				s => s && s.id && s.id.startsWith('http')
			)
			if (directServer) {
				return directServer.id.trim()
			}

			// 3. OK.ru server
			const okServer = data.servers.find(
				s => s && s.name && s.name.toLowerCase() === 'ok'
			)
			if (okServer && okServer.id) {
				return `https://ok.ru/video/${okServer.id.trim()}`
			}

			// 4. Arab HD / estream / Red HD video server
			const arabOrStream = data.servers.find(
				s =>
					s &&
					s.id &&
					(s.name.toLowerCase().includes('arab') ||
						s.name.toLowerCase().includes('estream') ||
						s.name.toLowerCase().includes('red'))
			)
			if (arabOrStream && arabOrStream.id) {
				return `https://vidshar.org/${arabOrStream.id.trim()}`
			}

			// 5. Fallback across any available server entry
			for (const server of data.servers) {
				if (server && server.id) {
					const id = server.id.trim()
					return id.startsWith('http')
						? id
						: `https://vidshar.org/${id}`
				}
			}
		}

		return null
	} catch (err) {
		logger.error(`Error extracting dailymotion link: ${err.message}`)
		return null
	}
}

/**
 * @source https://stackoverflow.com/questions/42404291
 * @param {number} time time in seconds
 * @returns {string} return server uptime
 */
const uptime = time => {
	const date = new Date(time * 1000)

	const days = date.getUTCDate() - 1,
		hours = date.getUTCHours(),
		minutes = date.getUTCMinutes(),
		seconds = date.getUTCSeconds()

	let segments = []

	if (days > 0) segments.push(days + ' day' + (days == 1 ? '' : 's'))
	if (hours > 0) segments.push(hours + ' hour' + (hours == 1 ? '' : 's'))
	if (minutes > 0)
		segments.push(minutes + ' minute' + (minutes == 1 ? '' : 's'))
	if (seconds > 0)
		segments.push(seconds + ' second' + (seconds == 1 ? '' : 's'))
	const dateString = segments.join(', ')
	return dateString
}

/**
 *
 * @param {string} url url to be converted
 * @returns {string} converted url
 */
const convertUrl = url => {
	try {
		if (!url || !url.includes('=')) return url || ''
		const urlParam = url.split('=')[1].replace(/%3D/g, '')
		const decodedUrl = decodeURI(base64.decode(urlParam))
		return decodedUrl.endsWith('/') ? decodedUrl.slice(0, -1) : decodedUrl
	} catch (err) {
		logger.error(`Error converting url ${url}: ${err.message}`)
		return url
	}
}

/**
 * @desc fetch latest episodes from qeseh.net/son-bolumler
 * @param {number} count number of episodes to fetch
 * @returns {Promise<Array>}
 */
const fetchLatestEpisodes = async (count = 5) => {
	const cacheKey = `latest:episodes:${count}`
	if (await redisClient.exists(cacheKey)) {
		logger.info(`Cache hit for ${cacheKey}`)
		try {
			return JSON.parse(await redisClient.get(cacheKey))
		} catch (err) {
			logger.error(`Error parsing cached latest episodes: ${err.message}`)
		}
	}

	logger.info(`Cache miss for ${cacheKey} - scraping qeseh.net/son-bolumler`)
	try {
		const res = await axios.get('https://qeseh.net/son-bolumler/', {
			headers: { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)' },
			timeout: 8000
		})
		const $ = cheerio.load(res.data)
		const items = []

		$('#load-post-episodes article.post')
			.slice(0, count)
			.each((i, el) => {
				const a = $(el).find('a').first()
				const href = a.attr('href') || ''
				const rawTitle = a.attr('title') || $(el).find('.title').text().trim()
				const title = rawTitle.replace(/\s*-\s*قصة عشق\s*$/, '').trim()
				const epMatch = title.match(/الحلقة\s*(\d+)/)
				const epNum =
					$(el).find('.episodeNum span:last-child').text().trim() ||
					(epMatch ? epMatch[1] : '')
				const seriesTitle = title
					.replace(/الحلقة\s*\d+.*$/, '')
					.replace(/مسلسل\s*/, '')
					.trim()
				const imgBg = $(el).find('.imgBg').attr('style') || ''
				let thumb = ''
				if (imgBg.includes('url(')) {
					thumb = imgBg.split('url(')[1].split(')')[0].replace(/['"]/g, '')
				} else {
					thumb = $(el).find('img').attr('src') || ''
				}

				items.push({
					id: href.split('/')[4] || `ep-${i + 1}`,
					title,
					seriesTitle,
					episode: epNum,
					url: href,
					thumb
				})
			})

		// Resolve streaming links in parallel
		const resolved = await Promise.all(
			items.map(async item => {
				try {
					const epRes = await axios.get(item.url, {
						headers: {
							'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'
						},
						timeout: 5000
					})
					const streamLink = extractDailymotionLinks(epRes.data)
					return { ...item, streamLink }
				} catch (err) {
					return { ...item, streamLink: null }
				}
			})
		)

		// Cache for 10 minutes (600s)
		await redisClient.setEx(cacheKey, 600, JSON.stringify(resolved))
		return resolved
	} catch (err) {
		logger.error(`Error fetching latest episodes: ${err.message}`)
		return []
	}
}

module.exports = {
	fetchHtml,
	fetchEpisodesHtml,
	fetchDailyLinks,
	extractSeriesInfos,
	extractEpisodesUrls,
	extractDailymotionLinks,
	fetchLatestEpisodes,
	search,
	uptime
}
