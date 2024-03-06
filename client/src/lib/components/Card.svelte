<script>
	import toast from 'svelte-french-toast'

	/** @type {string} */
	export let url = ''
	/** @type {string} */
	export let cover = ''
	/** @type {string} */
	export let title = ''
	/** @type {string} */
	export let episode = ''

	const handleCopy = () => {
		navigator.clipboard.writeText(url)
		toast.success('Copied to clipboard!', {
			duration: 2000,
			position: 'top-center'
		})
	}
</script>

<!-- Card -->
<div class="group relative block">
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
						class="inline-flex items-center gap-1.5 rounded-full bg-red-500 px-3 py-1.5 font-ibx text-xs font-semibold text-gray-200 dark:bg-red-700"
					>
						{episode} حلقة
					</span>

					<button
						on:click={handleCopy}
						class="ml-[14.7rem] inline-flex items-center gap-1.5 rounded-full bg-purple-500 px-4 py-1.5 font-ibx text-xs font-semibold text-gray-200 transition-all duration-300 ease-in-out hover:bg-purple-600 dark:bg-purple-700"
					>
						📋
					</button>
				</div>
			</div>
		</div>
	</div>
	<!-- End Episode -->

	<div class="absolute inset-x-0 bottom-0 z-10">
		<div class="flex h-full flex-col p-4 sm:p-6">
			<p class="mt-2 font-ibx text-sm text-white/[.8]">{title}</p>
		</div>
	</div>
</div>
<!-- End Card -->
