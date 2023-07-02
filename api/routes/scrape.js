const router = require('express').Router()

const { scrape } = require('../controllers/main.controller')

router.get('/:id', scrape)

module.exports = { router }
