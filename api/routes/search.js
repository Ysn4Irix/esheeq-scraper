const router = require('express').Router()

const { search } = require('../controllers/main.controller')

router.get('/:q', search)

module.exports = { router }
