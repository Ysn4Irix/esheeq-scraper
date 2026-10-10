import { API_URL } from '$lib/constants.js'

/** @type {import('./$types').PageServerLoad} */
export const load = async ({ fetch }) => {
	try {
		const res = await fetch(`${API_URL}/latest?count=5`)
		if (res.ok) {
			const json = await res.json()
			return {
				latestEpisodes: json?.data?.episodes || []
			}
		}
	} catch (err) {
		console.error('Failed to load latest episodes on SSR:', err)
	}

	return {
		latestEpisodes: []
	}
}
