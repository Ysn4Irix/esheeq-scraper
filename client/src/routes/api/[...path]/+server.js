import { env } from '$env/dynamic/private'

/** @type {import('./$types').RequestHandler} */
export const GET = async ({ params, url, request }) => {
	const backendBase =
		env.API_INTERNAL_URL ||
		process.env.API_INTERNAL_URL ||
		'http://localhost:4000/api'

	const targetUrl = `${backendBase.replace(/\/$/, '')}/${params.path}${url.search}`

	try {
		const upstreamResponse = await fetch(targetUrl, {
			headers: {
				accept: request.headers.get('accept') || 'application/json'
			}
		})

		const data = await upstreamResponse.arrayBuffer()
		const headers = new Headers()
		const contentType = upstreamResponse.headers.get('content-type')
		if (contentType) {
			headers.set('content-type', contentType)
		}

		return new Response(data, {
			status: upstreamResponse.status,
			statusText: upstreamResponse.statusText,
			headers
		})
	} catch (err) {
		return new Response(
			JSON.stringify({
				message: 'Backend service unreachable',
				error: true,
				code: 502,
				timestamp: new Date()
			}),
			{
				status: 502,
				headers: { 'content-type': 'application/json' }
			}
		)
	}
}
