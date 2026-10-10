<script>
	import Card from '$lib/components/Card.svelte'
	import Nav from '$lib/components/Nav.svelte'
	import toast from 'svelte-french-toast'

	/** @type {import('./$types').PageData} */
	export let data

	/** @type {number} */
	let first = 16

	let copiedAll = false

	const copyAllLinks = async () => {
		const links = (data?.results?.data?.dailyLinks || [])
			.filter(item => item && item.link)
			.map(item => `الحلقة ${item.episode}: ${item.link}`)
			.join('\n')

		if (!links) {
			toast.error('لا توجد روابط قابلة للنسخ حالياً', {
				style: 'background: #161B22; color: #F85149; border: 1px solid #F85149; font-family: Tajawal, sans-serif; font-size: 13px;'
			})
			return
		}

		try {
			if (navigator?.clipboard?.writeText) {
				await navigator.clipboard.writeText(links)
			} else {
				const textarea = document.createElement('textarea')
				textarea.value = links
				textarea.style.position = 'fixed'
				textarea.style.opacity = '0'
				document.body.appendChild(textarea)
				textarea.focus()
				textarea.select()
				document.execCommand('copy')
				document.body.removeChild(textarea)
			}
			copiedAll = true
			setTimeout(() => { copiedAll = false }, 2500)
			toast.success(`تم نسخ جميع الروابط (${data?.results?.data?.dailyLinks?.length || 0} رابط) بنجاح`, {
				duration: 3000,
				style: 'background: #161B22; color: #3FB950; border: 1px solid #2EA043; font-family: Tajawal, sans-serif; font-size: 13px;'
			})
		} catch (err) {
			console.error('Failed to copy links:', err)
			// Fallback copy attempt
			try {
				const textarea = document.createElement('textarea')
				textarea.value = links
				textarea.style.position = 'fixed'
				textarea.style.opacity = '0'
				document.body.appendChild(textarea)
				textarea.focus()
				textarea.select()
				document.execCommand('copy')
				document.body.removeChild(textarea)
				copiedAll = true
				setTimeout(() => { copiedAll = false }, 2500)
				toast.success(`تم نسخ جميع الروابط (${data?.results?.data?.dailyLinks?.length || 0} رابط) بنجاح`, {
					duration: 3000,
					style: 'background: #161B22; color: #3FB950; border: 1px solid #2EA043; font-family: Tajawal, sans-serif; font-size: 13px;'
				})
			} catch (e) {
				toast.error('تعذر النسخ، يرجى المحاولة يدوياً', {
					style: 'background: #161B22; color: #F85149; border: 1px solid #F85149; font-family: Tajawal, sans-serif; font-size: 13px;'
				})
			}
		}
	}

	$: cleanActors = (data?.results?.data?.actors || []).filter(actor => {
		if (!actor || typeof actor !== 'string') return false
		const trimmed = actor.trim()
		if (trimmed.length < 2) return false
		const lower = trimmed.toLowerCase()
		const junkKeywords = ['حلقات', 'مترجم', 'مشاهدة', 'طاقم عمل', 'طاقم العمل', 'مسلسل', 'اون لاين', 'قصة عشق', 'بجودة']
		return !junkKeywords.some(keyword => lower.includes(keyword))
	})
</script>

<svelte:head>
	<title>{data?.results?.data?.title || 'مسلسل'} // قصة عشق</title>
</svelte:head>

<Nav arrowBack={true} />

<main class="mx-auto max-w-5xl px-4 py-8 sm:px-6">
	{#if !data?.results || data.results.code !== 200 || !data.results.data}
		<div class="rounded-lg border border-[#F85149]/40 bg-[#161B22] p-12 text-center">
			<div class="text-[#F85149] text-base font-bold mb-2 font-mono">[خطأ: غير موجود]</div>
			<p class="text-[#8B949E] text-xs font-sans">لم يتم العثور على المسلسل المطلوب في الأرشيف.</p>
			<a
				href="/"
				class="inline-block mt-4 rounded border border-[#30363D] bg-[#21262D] px-4 py-1.5 text-xs text-[#58A6FF] hover:border-[#58A6FF]"
			>
				<span>العودة إلى شاشة البحث ←</span>
			</a>
		</div>
	{:else}
		<!-- Series Metadata Header (Polished Terminal Card) -->
		<section class="mb-6 sm:mb-8 rounded-lg border border-[#30363D] bg-[#161B22] shadow-xl overflow-hidden">
			<!-- Terminal Window Header -->
			<div class="flex flex-wrap items-center justify-between gap-2 border-b border-[#30363D] bg-[#0D1117] px-4 py-2.5 text-xs">
				<div class="flex items-center gap-2">
					<span class="inline-block h-2 w-2 rounded-full bg-[#2EA043] animate-pulse"></span>
					<span class="font-mono text-[#8B949E] text-[11px]">فهرس المسلسلات // بيانات العمل</span>
				</div>
				<div class="flex items-center gap-2 font-mono text-[11px]">
					<span class="rounded bg-[#21262D] border border-[#30363D] px-2 py-0.5 text-[#3FB950] font-bold">
						● مكتمل
					</span>
					<span class="rounded bg-[#21262D] border border-[#30363D] px-2 py-0.5 text-[#58A6FF] font-medium">
						{data.results.data.dailyLinks?.length || 0} حلقة
					</span>
				</div>
			</div>

			<!-- Card Content Body -->
			<div class="p-4 sm:p-6">
				<div class="flex flex-col md:flex-row gap-5 md:gap-7 items-start">
					<!-- Poster Frame -->
					{#if data.results.data.cover}
						<div class="w-full md:w-52 flex justify-center md:block flex-shrink-0">
							<div class="relative w-40 xs:w-48 md:w-full aspect-[3/4] rounded-lg overflow-hidden border border-[#30363D] bg-[#0D1117] shadow-xl group">
								<img
									src={data.results.data.cover}
									alt={data.results.data.title}
									class="w-full h-full object-cover transition duration-300 group-hover:scale-105"
								/>
								<span class="absolute bottom-2 right-2 rounded bg-[#0D1117]/90 border border-[#30363D] px-2 py-0.5 font-mono text-[10px] text-[#3FB950] font-semibold backdrop-blur-sm">
									1080p FHD
								</span>
							</div>
						</div>
					{/if}

					<!-- Metadata Column -->
					<div class="flex-1 space-y-4 w-full min-w-0">
						<!-- Title & Quick Stats -->
						<div>
							<h1 class="font-sans text-xl sm:text-2xl md:text-3xl font-extrabold text-[#F0F6FC] leading-snug">
								{data.results.data.title}
							</h1>
							<div class="flex flex-wrap items-center gap-2 mt-2 text-xs text-[#8B949E] font-mono">
								<span class="rounded bg-[#21262D] px-2 py-0.5 border border-[#30363D] text-[#C9D1D9]">مسلسل</span>
								<span>•</span>
								<span>الجودة: 1080p Web-DL</span>
								<span>•</span>
								<span class="text-[#3FB950] font-bold">{data.results.data.dailyLinks?.length || 0} حلقة متوفرة</span>
							</div>
						</div>

						<!-- Description with elegant accent bar -->
						{#if data.results.data.description}
							<div class="rounded border-r-2 border-[#58A6FF] bg-[#0D1117]/30 pr-3 pl-2 py-2">
								<p class="font-sans text-xs sm:text-sm text-[#8B949E] leading-relaxed">
									{data.results.data.description}
								</p>
							</div>
						{/if}

						<!-- Cleaned Cast Tags -->
						{#if cleanActors && cleanActors.length > 0}
							<div>
								<span class="text-[11px] text-[#8B949E] block mb-1.5 font-medium">طاقم التمثيل:</span>
								<div class="flex flex-wrap gap-1.5">
									{#each cleanActors as actor}
										<span class="font-sans text-[11px] rounded bg-[#21262D] px-2.5 py-1 text-[#C9D1D9] border border-[#30363D] hover:border-[#58A6FF] transition-colors">
											{actor}
										</span>
									{/each}
								</div>
							</div>
						{/if}

						<!-- Bulk Copy Command Ribbon -->
						<div class="pt-3 border-t border-[#30363D]">
							<div class="rounded-lg border border-[#30363D] bg-[#0D1117]/60 p-3.5 sm:p-4 flex flex-col md:flex-row items-stretch md:items-center justify-between gap-3.5">
								<div class="flex items-center gap-3 min-w-0">
									<div class="flex h-10 w-10 items-center justify-center rounded-lg bg-[#2EA043]/15 border border-[#2EA043]/30 text-[#3FB950] flex-shrink-0">
										<svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
											<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7v8a2 2 0 002 2h6M8 7V5a2 2 0 012-2h4.586a1 1 0 01.707.293l4.414 4.414a1 1 0 01.293.707V15a2 2 0 01-2 2h-2M8 7H6a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2v-2" />
										</svg>
									</div>
									<div class="min-w-0">
										<div class="font-sans font-bold text-xs sm:text-sm text-[#F0F6FC] leading-normal">
											استخراج فوري لجميع الروابط
										</div>
										<div class="text-[11px] font-mono text-[#8B949E] leading-normal mt-0.5 truncate">
											{data.results.data.dailyLinks?.length || 0} رابط مباشر جاهز للنسخ (1080p FHD)
										</div>
									</div>
								</div>

								<button
									type="button"
									on:click={copyAllLinks}
									class="w-full md:w-auto min-h-[44px] inline-flex items-center justify-center gap-2 rounded-lg border border-[#2EA043] bg-[#2EA043] px-6 py-2.5 text-xs sm:text-sm font-bold text-white shadow-md shadow-[#2EA043]/20 transition-all hover:bg-[#2c973f] hover:shadow-lg hover:shadow-[#2EA043]/30 active:scale-95 whitespace-nowrap flex-shrink-0"
								>
									{#if copiedAll}
										<svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
											<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7" />
										</svg>
										<span>تم نسخ جميع الروابط!</span>
									{:else}
										<svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
											<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 5H6a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2v-1M8 5a2 2 0 002 2h2a2 2 0 002-2M8 5a2 2 0 012-2h2a2 2 0 012 2m0 0h2a2 2 0 012 2v3m2 4H10m0 0l3-3m-3 3l3 3" />
										</svg>
										<span>نسخ جميع الروابط دفعة واحدة</span>
									{/if}
								</button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</section>

		<!-- Episode Grid Section -->
		<section class="space-y-4">
			<div class="flex items-center justify-between border-b border-[#30363D] pb-2 text-xs">
				<span class="text-[#F0F6FC]">سجل الحلقات // اختر الحلقة للنسخ</span>
				<span class="text-[#8B949E] font-mono">
					عرض {Math.min(first, data.results.data.dailyLinks?.length || 0)} من أصل {data.results.data.dailyLinks?.length || 0} حلقة
				</span>
			</div>

			<div class="grid grid-cols-2 gap-2.5 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
				{#each (data.results.data.dailyLinks || []).slice(0, first) as item}
					<Card
						title={data.results.data.title}
						url={item.link}
						cover={data.results.data.cover}
						episode={item.episode}
					/>
				{/each}
			</div>

			<!-- Pagination: Load More Button -->
			{#if first < (data.results.data.dailyLinks?.length || 0)}
				<div class="pt-6 pb-2 text-center">
					<button
						type="button"
						on:click={() => (first += 16)}
						class="w-full sm:w-auto min-h-[44px] inline-flex items-center justify-center gap-2 rounded border border-[#30363D] bg-[#21262D] px-6 py-2.5 text-xs font-semibold text-[#58A6FF] transition hover:border-[#58A6FF] hover:text-[#79C0FF] active:scale-95"
					>
						<span>[+] عرض المزيد من الحلقات ({Math.min(16, data.results.data.dailyLinks.length - first)} متبقية)</span>
					</button>
				</div>
			{/if}
		</section>
	{/if}
</main>
