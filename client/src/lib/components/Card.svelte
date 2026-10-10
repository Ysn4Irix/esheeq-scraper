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

	let copied = false

	const handleCopy = () => {
		if (!url) {
			toast.error('رابط الحلقة غير متاح حالياً', {
				style: 'background: #161B22; color: #F85149; border: 1px solid #F85149; font-family: Tajawal, sans-serif; font-size: 13px;'
			})
			return
		}
		navigator.clipboard.writeText(url)
		copied = true
		setTimeout(() => { copied = false }, 2000)
		toast.success(`تم نسخ رابط الحلقة ${episode}`, {
			duration: 2000,
			style: 'background: #161B22; color: #3FB950; border: 1px solid #2EA043; font-family: Tajawal, sans-serif; font-size: 13px;'
		})
	}

	const handlePlay = () => {
		if (!url) {
			toast.error('رابط الحلقة غير متاح حالياً', {
				style: 'background: #161B22; color: #F85149; border: 1px solid #F85149; font-family: Tajawal, sans-serif; font-size: 13px;'
			})
			return
		}
		window.open(url, '_blank', 'noopener,noreferrer')
	}
</script>

<div class="overflow-hidden rounded-lg border border-[#30363D] bg-[#161B22] p-2.5 sm:p-3 transition-all hover:border-[#58A6FF] hover:shadow-[0_0_12px_rgba(88,166,255,0.1)] flex flex-col justify-between">
	<div>
		{#if cover}
			<div class="relative aspect-[16/9] w-full rounded mb-2 overflow-hidden bg-[#0D1117] border border-[#30363D]">
				<img src={cover} alt={title} class="w-full h-full object-cover" loading="lazy" />
				<span class="absolute top-1 right-1 sm:top-1.5 sm:right-1.5 rounded border border-[#30363D] bg-[#0D1117]/90 px-1.5 py-0.5 text-[#3FB950] font-bold text-[9px] sm:text-[10px]">
					حلقة {episode}
				</span>
			</div>
		{:else}
			<div class="flex items-center justify-between mb-2 text-xs">
				<span class="rounded border border-[#30363D] bg-[#0D1117] px-2 py-0.5 text-[#3FB950] font-bold text-[10px] sm:text-xs">
					حلقة {episode}
				</span>
				<span class="text-[10px] text-[#8B949E] font-mono">1080p</span>
			</div>
		{/if}
		<div class="font-sans text-xs text-[#C9D1D9] line-clamp-1 mb-2.5" title={title}>
			{title}
		</div>
	</div>

	<!-- Dual Action Buttons: Watch in New Tab + Copy Link -->
	<div class="grid grid-cols-2 gap-1.5 pt-1">
		<button
			on:click={handlePlay}
			type="button"
			class="min-h-[38px] inline-flex items-center justify-center gap-1 rounded border border-[#2EA043] bg-[#2EA043] py-1.5 px-2 text-[11px] sm:text-xs font-bold text-white shadow transition-all hover:bg-[#2c973f] hover:shadow-[#2EA043]/30 active:scale-95 whitespace-nowrap"
			title="فتح الرابط للمشاهدة في علامة تبويب جديدة"
		>
			<svg class="h-3.5 w-3.5 flex-shrink-0" fill="currentColor" viewBox="0 0 24 24">
				<path d="M8 5v14l11-7z"/>
			</svg>
			<span>مشاهدة</span>
		</button>

		<button
			on:click={handleCopy}
			type="button"
			class="min-h-[38px] inline-flex items-center justify-center gap-1 rounded border border-[#30363D] bg-[#21262D] py-1.5 px-2 text-[11px] sm:text-xs font-semibold transition active:scale-95 whitespace-nowrap {copied ? 'text-[#3FB950] border-[#2EA043] bg-[#2EA043]/10' : 'text-[#58A6FF] hover:border-[#58A6FF] hover:bg-[#30363D] hover:text-[#79C0FF]'}"
			title="نسخ رابط الحلقة"
		>
			{#if copied}
				<svg class="h-3.5 w-3.5 text-[#3FB950] flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
				</svg>
				<span>تم النسخ!</span>
			{:else}
				<svg class="h-3.5 w-3.5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 5H6a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2v-1M8 5a2 2 0 002 2h2a2 2 0 002-2M8 5a2 2 0 012-2h2a2 2 0 012 2m0 0h2a2 2 0 012 2v3m2 4H10m0 0l3-3m-3 3l3 3" />
				</svg>
				<span>نسخ</span>
			{/if}
		</button>
	</div>
</div>
