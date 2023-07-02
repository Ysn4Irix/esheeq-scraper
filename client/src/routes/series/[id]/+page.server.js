import { API_URL } from '$lib/constants.js'

/** @type {import('./$types').PageServerLoad} */
export const load = async ({ params }) => {
	const { id } = params

	const res = await fetch(`${API_URL}/scrape/${id}`)
	const results = await res.json()

	return {
		results
	}
}
