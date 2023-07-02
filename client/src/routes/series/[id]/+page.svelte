<script>
	import { fly } from 'svelte/transition'
	import Card from '$lib/components/Card.svelte'
	import Nav from '$lib/components/Nav.svelte'

	/** @type {import('./$types').PageData} */
	export let data

	/** @type {number} */
	let first = 12
</script>

<svelte:head>
	<!-- Primary Meta Tags -->
	<title>{data.results.data.title}</title>
	<meta name="title" content={data.results.data.title} />
</svelte:head>

<!-- <pre>
	{JSON.stringify(data, null, 2)}
</pre> -->

<Nav arrowBack={true} />
<!-- Card Blog -->
<div class="mx-auto items-center px-4 py-4 sm:px-6 lg:py-4 xl:py-3">
	{#if data.results.code !== 200}
		<div class="mx-auto mb-5 max-w-2xl text-center lg:mb-7">
			<h2 class="text-2xl font-bold dark:text-white md:text-4xl md:leading-tight">
				Series not found
			</h2>
		</div>
	{:else}
		<!-- Infos -->
		<div class="mx-auto mb-5 max-w-2xl text-center lg:mb-7">
			<h2
				class="font-ibx text-3xl font-black text-red-500 dark:text-red-700 md:text-4xl md:leading-tight"
			>
				{data.results.data.title}
			</h2>
			<p class="font-ibx mt-3 text-lg text-gray-700 dark:text-gray-400">
				{data.results.data.description}
			</p>
			<p class="mt-3 text-gray-600 dark:text-gray-400">
				<span class="font-ibx text-md font-bold text-red-500">الممثلون :</span>
				{#each data.results.data.actors as actor}
					<span class="font-ibx text-gray-700 dark:text-gray-400">
						,{actor}
					</span>
				{/each}
			</p>
		</div>
		<!-- End Infos -->

		<!-- Grid -->
		<ul
			class="grid grid-cols-1 gap-6 sm:grid-cols-1 sm:place-items-center md:grid-cols-2 lg:grid-cols-2 xl:grid-cols-4"
		>
			{#each data.results.data.dailyLinks.slice(0, first) as item}
				<li
					in:fly={{ y: 50, duration: 500, delay: 500 }}
					out:fly={{ duration: 500 }}
					data-sveltekit-noscroll
					id={item.episode}
				>
					<Card
						title={data.results.data.title}
						url={item.link}
						cover={data.results.data.cover}
						episode={item.episode}
					/>
				</li>
			{/each}
		</ul>
		<!-- End Grid -->
		<!-- Load more -->
		{#if first < data.results.data.dailyLinks.length}
			<div class="my-5 flex justify-center">
				<button
					type="button"
					class="inline-flex items-center justify-center gap-2 rounded-md bg-red-500 px-6 py-3 align-middle text-sm font-medium text-gray-200 shadow-md transition-all duration-300 ease-in-out hover:bg-red-600 dark:bg-red-700 dark:text-gray-300 dark:hover:bg-red-800"
					on:click={() => (first += 4)}
				>
					Load more
					<svg
						xmlns="http://www.w3.org/2000/svg"
						viewBox="0 0 20 20"
						fill="currentColor"
						class="h-5 w-5"
					>
						<path
							fill-rule="evenodd"
							d="M10 3a.75.75 0 01.75.75v10.638l3.96-4.158a.75.75 0 111.08 1.04l-5.25 5.5a.75.75 0 01-1.08 0l-5.25-5.5a.75.75 0 111.08-1.04l3.96 4.158V3.75A.75.75 0 0110 3z"
							clip-rule="evenodd"
						/>
					</svg>
				</button>
			</div>
		{/if}
		<!-- End Load more -->
	{/if}
</div>
<!-- End Card Blog -->
