const router = require('express').Router()
const { latest } = require('../controllers/main.controller')

router.get('/', latest)

module.exports = { router }
