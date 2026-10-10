<script>
	/** @type {string} */
	export let url = ''
	/** @type {string} */
	export let cover = ''
	/** @type {string} */
	export let title = ''
</script>

<a
	data-sveltekit-preload-data="hover"
	href={url}
	class="group block overflow-hidden rounded-lg border border-[#30363D] bg-[#161B22] transition-all hover:border-[#58A6FF] hover:shadow-[0_0_15px_rgba(88,166,255,0.12)] text-xs"
>
	<div class="relative aspect-[16/10] w-full overflow-hidden bg-[#0D1117]">
		{#if cover}
			<img
				class="h-full w-full object-cover transition duration-300 group-hover:scale-105"
				src={cover}
				alt={title}
				loading="lazy"
			/>
		{:else}
			<div class="flex h-full w-full items-center justify-center text-[#8B949E] text-[11px]">
				[لا يوجد غلاف]
			</div>
		{/if}
		<div class="absolute inset-0 bg-gradient-to-t from-[#161B22] via-transparent to-transparent opacity-80"></div>
	</div>

	<div class="p-3">
		<h3 class="font-sans text-sm font-bold text-[#F0F6FC] group-hover:text-[#58A6FF] transition line-clamp-1">
			{title}
		</h3>
		<div class="mt-2 flex items-center justify-between text-[11px] text-[#8B949E] border-t border-[#21262D] pt-2">
			<span>مسلسل</span>
			<span class="text-[#58A6FF] group-hover:underline font-semibold flex items-center gap-1">
				<span>عرض الحلقات</span>
				<span>←</span>
			</span>
		</div>
	</div>
</a>
