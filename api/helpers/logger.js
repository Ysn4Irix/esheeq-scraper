const logger = require('signale')

logger.config({
	displayFilename: true,
	displayTimestamp: true,
	displayBadge: false,
	displayDate: false
})

module.exports = logger
