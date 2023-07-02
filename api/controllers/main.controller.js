const {
	uptime,
	fetchHtml,
	extractSeriesInfos,
	extractEpisodesUrls,
	search,
	fetchDailyLinks
} = require('../helpers/utils')
const { createWriteStream, unlink } = require('fs')
const { join } = require('path')
const { success, error } = require('../helpers/apiResponse')
const logger = require('../helpers/logger')
const { base_url } = require('../helpers/constants')

module.exports = {
	/**
	 * @desc check whatever the server is up or not
	 * @param {import('@types/express').Request} _
	 * @param {import('@types/express').Response} res
	 * @returns {object} object
	 */
	alive: (_, res) => {
		try {
			res.status(200).json(
				success("🎉I'm alive", {
					upTime: uptime(process.uptime())
				})
			)
		} catch (err) {
			logger.error(err.message)
			res.status(500).json(
				error(
					'Oops! We have an problem in our backend 😢',
					res.statusCode
				)
			)
		}
	},
	/**
	 * @desc check whatever the server is up or not
	 * @param {import('@types/express').Request} req
	 * @param {import('@types/express').Response} res
	 * @returns {object} object
	 */
	scrape: async (req, res) => {
		try {
			const { id } = req.params
			const { raw } = req.query

			const htmlData = await fetchHtml(`${base_url}/series/${id}`)

			const seriesInfos = extractSeriesInfos(htmlData)

			const episodesUrls = extractEpisodesUrls(htmlData)

			const dailyLinks = await fetchDailyLinks(
				seriesInfos.id,
				episodesUrls
			)

			if (raw === 'true') {
				let links = ''
				dailyLinks.forEach(item => {
					links += `<span style='display: block;'>${item.episode} => ${item.link}</span>`
				})
				res.status(200).send(links)
			} else {
				res.status(200).json(
					success(
						'✅ Done',
						{ ...seriesInfos, dailyLinks },
						res.statusCode
					)
				)
			}
		} catch (err) {
			logger.error(err.message)
			res.status(500).json(
				error(
					'Oops! We have an problem in our backend 😢',
					res.statusCode
				)
			)
		}
	},
	/**
	 * @desc check whatever the server is up or not
	 * @param {import('@types/express').Request} req
	 * @param {import('@types/express').Response} res
	 * @returns {object} object
	 */
	search: async (req, res) => {
		const { q } = req.params

		const results = await search(q)

		res.status(200).json(
			success(
				'✅ Done',
				{
					results
				},
				res.statusCode
			)
		)
	}
}
