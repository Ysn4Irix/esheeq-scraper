/**
 * @desc    Send any success response
 *
 * @param   {string} message
 * @param   {object | array} data
 * @param   {number} statusCode
 * @returns {object} object
 */

const success = (message, data, statusCode) => {
	return {
		message,
		error: false,
		code: statusCode,
		data
	}
}

/**
 * @desc    Send any error response
 *
 * @param   {string} message
 * @param   {number} statusCode
 * @returns {object} object
 */

const error = (message, statusCode) => {
	const codes = [200, 201, 400, 401, 404, 403, 422, 500]

	const codeFinder = codes.find(code => code === statusCode)

	if (!codeFinder) statusCode = 500
	else statusCode = codeFinder

	return {
		message,
		code: statusCode,
		error: true,
		timestamp: new Date()
	}
}

module.exports = {
	success,
	error
}
