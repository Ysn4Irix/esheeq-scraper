const router = require('express').Router()

const { alive } = require('../controllers/main.controller')

router.get('/', alive)

module.exports = { router }
