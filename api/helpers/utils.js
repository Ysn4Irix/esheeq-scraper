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
 * @param {string} key redis key
 * @param {string} seriesId series id
 * @param {array<string>} episodesUrls array of episodes urls
 * @returns {Promise<array>} array of daily links
 */
const fetchDailyLinks = async (seriesId, episodesUrls) => {
	const dailyLinksKey = `dailyLinks:${seriesId}`

	let dailyLinks = []
	if (await redisClient.exists(dailyLinksKey)) {
		logger.info(`Cache hit for ${dailyLinksKey}`)
		dailyLinks = JSON.parse(await redisClient.get(dailyLinksKey))
	} else {
		logger.info(`Cache miss for ${dailyLinksKey}`)
		for (let i = 0; i < episodesUrls.length; i++) {
			const html = await fetchEpisodesHtml(seriesId, episodesUrls[i])
			if (html) {
				const dailyLink = extractDailymotionLinks(html)
				dailyLinks.push({
					episode: `${i + 1}`,
					link: dailyLink
				})
			}
		}
	}
	redisClient.setEx(dailyLinksKey, 86400, JSON.stringify(dailyLinks))
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
			const url = decodeURI($(el).find('a').attr('href'))
			const id = url.split('/')[4]
			const cover = $(el)
				.find('div.imgBg')
				.attr('style')
				.split('(')[1]
				.replace(');', '')
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

	infos.title = infoElement.find('h1').text()
	infos.id = infos.title.replace(/ /g, '-')
	const cover = coverElement.find('div.img').attr('style')
	infos.cover = cover.split('(')[1].replace(');', '')
	infos.description = infoElement.find('div.story').text()
	infoElement.find('div.tax > a').each((i, el) => {
		const actor = $(el).attr('title')
		infos.actors.push(actor)
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
 * @desc extract dailymotion links from the episodes pages
 * @param {string} html
 * @returns {string} dailymotion link
 */
const extractDailymotionLinks = html => {
	const $ = cheerio.load(html)

	const element = $(
		'body > div.secContainer.bg > div > div > div.getEmbed > div > span > a'
	)
	const url = element.attr('href')
	const jsonData = url.split('=')[2].replace(/%3D/g, '')
	const decodedData = base64.decode(jsonData)
	const data = JSON.parse(decodedData)

	if (data.servers[0].id.includes('https://')) return data.servers[0].id
	else return `https://www.dailymotion.com/video/${data.servers[0].id}`
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
	const urlParam = url.split('=')[1].replace(/%3D/g, '')
	const decodedUrl = decodeURI(base64.decode(urlParam))
	const noLastSlashUrl = decodedUrl.slice(0, -1)
	return noLastSlashUrl
}

module.exports = {
	fetchHtml,
	fetchEpisodesHtml,
	fetchDailyLinks,
	extractSeriesInfos,
	extractEpisodesUrls,
	extractDailymotionLinks,
	search,
	uptime
}
