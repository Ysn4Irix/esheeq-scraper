<script>
	/** @type {string} */
	export let url = ''
	/** @type {string} */
	export let cover = ''
	/** @type {string} */
	export let title = ''
	/** @type {string} */
	export let episode = ''
</script>

<!-- Card -->
<a class="group relative block" href={url} target="_blank">
	<div
		class="relative h-[230px] w-[350px] flex-shrink-0 overflow-hidden rounded-xl shadow-lg before:absolute before:inset-x-0 before:z-[1] before:h-full before:w-full before:bg-gradient-to-t before:from-gray-900/[.7]"
	>
		<img
			class="absolute left-0 top-0 h-full w-full object-fill transition-all duration-300 ease-in-out group-hover:scale-110"
			src={cover}
			alt={title}
		/>
	</div>

	<!-- Episode -->
	<div class="absolute inset-x-0 top-0 z-10">
		<div class="flex h-full flex-col p-1 sm:p-1">
			<div class="flex items-center">
				<div class="flex-shrink-0">
					<span
						class="font-ibx inline-flex items-center gap-1.5 rounded-full bg-red-500 px-3 py-1.5 text-xs font-semibold text-gray-200 dark:bg-red-700"
					>
						{episode} حلقة
					</span>
				</div>
			</div>
		</div>
	</div>
	<!-- End Episode -->

	<div class="absolute inset-x-0 bottom-0 z-10">
		<div class="flex h-full flex-col p-4 sm:p-6">
			<p class="font-ibx mt-2 text-sm text-white/[.8]">{title}</p>
		</div>
	</div>
</a>
<!-- End Card -->
