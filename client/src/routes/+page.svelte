<script>
	import { API_URL } from '$lib/constants.js'
	import Nav from '$lib/components/Nav.svelte'

	/** @type {array<string>} */
	let searchResults = []
	/** @type {string} */
	let query = ''
	/** @type {string} */
	let errMsg = ''

	const handleSearch = async () => {
		if (query.length < 3) {
			searchResults = []
			errMsg = 'الرجاء إدخال 3 أحرف على الأقل'
			return
		}
		const response = await fetch(`${API_URL}/search/${query}`)
		if (response.ok && response.status === 200) {
			const { data } = await response.json()
			searchResults = data.results
			if (searchResults.length === 0) {
				errMsg = 'لم يتم العثور على أي نتيجة'
			}
		} else if (response.status === 500) {
			searchResults = []
			errMsg = 'Something went wrong, please try again later'
		}
	}
</script>

<Nav />
<!-- Hero -->
<div class="relative overflow-hidden">
	<div class="mx-auto max-w-[85rem] px-4 py-16 sm:px-6 sm:py-16 lg:px-8">
		<div class="text-center">
			<h1
				class="font-pacifico text-5xl font-semibold text-red-700 dark:text-red-500 sm:text-5xl md:text-6xl"
			>
				Esheeq
			</h1>

			<p class="mt-6 text-gray-600 dark:text-gray-400">
				<span class="font-pacifico font-semibold">Scrap</span> dailymotion episodes links for a
				given series directly from <span class="font-pacifico font-semibold">esheeq</span>
			</p>

			<div class="relative mx-auto mt-8 max-w-xl sm:mt-8">
				<div
					class="relative z-10 flex space-x-3 rounded-lg border bg-white p-3 shadow-lg shadow-gray-100 dark:border-gray-700 dark:bg-gray-800 dark:shadow-gray-900/[.2]"
				>
					<div class="flex-[1_0_0%]">
						<label for="query" class="block text-sm font-medium text-gray-700 dark:text-white">
							<span class="font-ibx sr-only text-right">إبحث عن مسلسل</span>
						</label>
						<input
							type="text"
							name="query"
							id="query"
							bind:value={query}
							on:input={handleSearch}
							class="font-ibx block w-full rounded-md border-transparent p-3 text-right focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-800 dark:text-gray-400"
							placeholder="... إبحث عن مسلسل"
						/>
					</div>
				</div>

				<!-- SVG Element -->
				<div class="absolute right-0 top-0 hidden -translate-y-12 translate-x-20 md:block">
					<svg
						class="h-auto w-16 text-orange-500"
						width="121"
						height="135"
						viewBox="0 0 121 135"
						fill="none"
						xmlns="http://www.w3.org/2000/svg"
					>
						<path
							d="M5 16.4754C11.7688 27.4499 21.2452 57.3224 5 89.0164"
							stroke="currentColor"
							stroke-width="10"
							stroke-linecap="round"
						/>
						<path
							d="M33.6761 112.104C44.6984 98.1239 74.2618 57.6776 83.4821 5"
							stroke="currentColor"
							stroke-width="10"
							stroke-linecap="round"
						/>
						<path
							d="M50.5525 130C68.2064 127.495 110.731 117.541 116 78.0874"
							stroke="currentColor"
							stroke-width="10"
							stroke-linecap="round"
						/>
					</svg>
				</div>
				<!-- End SVG Element -->

				<!-- SVG Element -->
				<div class="absolute bottom-0 left-0 hidden -translate-x-32 translate-y-10 md:block">
					<svg
						class="h-auto w-40 text-cyan-500"
						width="347"
						height="188"
						viewBox="0 0 347 188"
						fill="none"
						xmlns="http://www.w3.org/2000/svg"
					>
						<path
							d="M4 82.4591C54.7956 92.8751 30.9771 162.782 68.2065 181.385C112.642 203.59 127.943 78.57 122.161 25.5053C120.504 2.2376 93.4028 -8.11128 89.7468 25.5053C85.8633 61.2125 130.186 199.678 180.982 146.248L214.898 107.02C224.322 95.4118 242.9 79.2851 258.6 107.02C274.299 134.754 299.315 125.589 309.861 117.539L343 93.4426"
							stroke="currentColor"
							stroke-width="7"
							stroke-linecap="round"
						/>
					</svg>
				</div>
				<!-- End SVG Element -->
			</div>
			<div class="mt-10 sm:mt-20">
				{#if searchResults.length > 0}
					{#each searchResults as item}
						<a
							data-sveltekit-preload-data="hover"
							aria-label="Go to series page"
							id={item.id}
							class="m-1 inline-flex items-center justify-center gap-2 rounded-md border bg-white px-4 py-3 align-middle text-sm font-semibold text-gray-700 shadow-sm transition-all hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:ring-offset-2 focus:ring-offset-white dark:border-gray-700 dark:bg-slate-900 dark:text-gray-400 dark:hover:bg-slate-800 dark:hover:text-white dark:focus:ring-offset-gray-800"
							href="/series/{item.id}"
						>
							{item.title}
						</a>
					{/each}
				{:else}
					<p class:hidden={query === ''} class="text-1xl font-ibx text-gray-600 dark:text-gray-400">
						{errMsg}
					</p>
				{/if}
			</div>
		</div>
	</div>
</div>
<!-- End Hero -->
