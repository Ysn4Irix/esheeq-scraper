require('dotenv').config()
const express = require('express')
const helmet = require('helmet')
const cors = require('cors')
const responseTime = require('response-time')
const logger = require('./helpers/logger')
const { router } = require('./routes')
const redisClient = require('./db/redis')
const app = express()
const PORT = process.env.PORT || 4000

app.use(responseTime())
app.use(helmet())
app.use(
	cors({
		origin: '*',
		optionsSuccessStatus: 200
	})
)
app.use(express.json())
app.use(
	express.urlencoded({
		extended: false
	})
)
app.use(require('./middlewares/limiter'))

app.use('/api', router)

app.use(require('./middlewares/notFoundHandler'))

const server = app.listen(PORT, () => {
	logger.success(
		`🚀 Server started => listening on PORT: ${PORT} with processId: ${process.pid}`
	)
})

process.on('SIGINT', () => {
	logger.info('SIGINT signal received.')
	logger.info('Server is closing.')
	server.close(() => {
		logger.info('Server closed.')
		redisClient.quit()
		process.exit(0)
	})
})

process.on('SIGTERM', () => {
	logger.info('SIGTERM signal received.')
	logger.info('Server is closed.')
	server.close(() => {
		logger.info('Server closed.')
		redisClient.quit()
		process.exit(0)
	})
})

module.exports = app
