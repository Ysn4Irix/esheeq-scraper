<script>
	/** @type {string} */
	export let url = ''
	/** @type {string} */
	export let cover = ''
	/** @type {string} */
	export let title = ''
</script>

<!-- Card -->
<a data-sveltekit-preload-data="hover" class="group relative block" href={url}>
	<div
		class="relative h-[410px] w-[280px] flex-shrink-0 overflow-hidden rounded-xl shadow-lg before:absolute before:inset-x-0 before:z-[1] before:h-full before:w-full before:bg-gradient-to-t before:from-gray-900/[.7]"
	>
		<img
			class="absolute left-0 top-0 h-full w-full object-fill transition-all duration-300 ease-in-out group-hover:scale-110"
			src={cover}
			alt={title}
		/>
	</div>

	<div class="absolute inset-x-0 bottom-0 z-10">
		<div class="flex h-full flex-col p-4 sm:p-6">
			<p class="font-ibx mt-2 text-sm text-white/[.8]">{title}</p>
		</div>
	</div>
</a>
<!-- End Card -->
