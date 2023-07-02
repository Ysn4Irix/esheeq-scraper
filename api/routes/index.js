const router = require('express').Router()
const { readdirSync } = require('fs')

const ROUTER_PATH = `${__dirname}`

/**
 * @desc Get rid of extension in file file.js -> file
 * @param {string} filename
 * @returns {string}
 */
const cleanFileName = filename => {
	return filename.split('.').shift()
}

//! Function that will read or scan all files that exist in a folder
readdirSync(ROUTER_PATH).filter(fileName => {
	const cleanName = cleanFileName(fileName)
	if (cleanName !== 'index') {
		//! Dynamically importing & creating routers
		const routerModule = require(`./${cleanName}`)
		router.use(`/${cleanName}`, routerModule.router)
	}
})

module.exports = { router }
