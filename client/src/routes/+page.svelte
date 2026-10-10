<script>
	import { onMount } from 'svelte'
	import { API_URL } from '$lib/constants.js'
	import Nav from '$lib/components/Nav.svelte'
	import SearchCard from '$lib/components/SearchCard.svelte'
	import toast from 'svelte-french-toast'

	/** @type {import('./$types').PageData} */
	export let data

	/** @type {Array<any>} */
	let searchResults = []
	/** @type {string} */
	let query = ''
	/** @type {string} */
	let errMsg = ''
	/** @type {boolean} */
	let loading = false
	/** @type {HTMLInputElement} */
	let inputEl

	// Fallback to client-side fetch if SSR returned empty array
	let latestEpisodes = data?.latestEpisodes || []

	onMount(async () => {
		if (latestEpisodes.length === 0) {
			try {
				const res = await fetch(`${API_URL}/latest?count=5`)
				if (res.ok) {
					const json = await res.json()
					latestEpisodes = json?.data?.episodes || []
				}
			} catch (e) {
				console.error(e)
			}
		}

		// Keyboard shortcut handler: CTRL+K to focus search, ESC to clear
		const handleKeydown = (e) => {
			if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
				e.preventDefault()
				inputEl?.focus()
			} else if (e.key === 'Escape' && query) {
				query = ''
				searchResults = []
				errMsg = ''
			}
		}
		window.addEventListener('keydown', handleKeydown)
		return () => window.removeEventListener('keydown', handleKeydown)
	})

	let debounceTimer
	const handleInput = () => {
		clearTimeout(debounceTimer)
		if (query.trim().length === 0) {
			searchResults = []
			errMsg = ''
			loading = false
			return
		}
		if (query.trim().length < 2) {
			searchResults = []
			errMsg = 'أدخل حرفين على الأقل للبحث'
			loading = false
			return
		}
		loading = true
		errMsg = ''
		debounceTimer = setTimeout(executeSearch, 250)
	}

	const executeSearch = async () => {
		try {
			const res = await fetch(`${API_URL}/search/${encodeURIComponent(query.trim())}`)
			if (res.ok) {
				const json = await res.json()
				searchResults = json?.data?.results || []
				if (searchResults.length === 0) {
					errMsg = 'لم يتم العثور على أي نتائج مطابقة'
				}
			} else {
				errMsg = 'تعذر استرجاع نتائج البحث'
			}
		} catch (err) {
			errMsg = 'خطأ في الاتصال بالخادم'
		} finally {
			loading = false
		}
	}

	const copyStream = (url, title) => {
		if (!url) {
			toast.error('رابط المشاهدة غير متاح حالياً', {
				style: 'background: #161B22; color: #F85149; border: 1px solid #F85149; font-family: monospace; font-size: 12px;'
			})
			return
		}
		navigator.clipboard.writeText(url)
		toast.success(`تم نسخ الرابط بنجاح: ${title}`, {
			duration: 2500,
			style: 'background: #161B22; color: #3FB950; border: 1px solid #2EA043; font-family: Tajawal, sans-serif; font-size: 13px;'
		})
	}

	const playEpisode = (url) => {
		if (!url) {
			toast.error('رابط المشاهدة غير متاح حالياً', {
				style: 'background: #161B22; color: #F85149; border: 1px solid #F85149; font-family: Tajawal, sans-serif; font-size: 13px;'
			})
			return
		}
		window.open(url, '_blank', 'noopener,noreferrer')
	}

	const searchSeries = (seriesName) => {
		query = seriesName
		handleInput()
		inputEl?.focus()
	}
</script>

<svelte:head>
	<title>قصة عشق // أداة استخراج روابط الحلقات المباشرة</title>
</svelte:head>

<Nav />

<main class="mx-auto max-w-4xl px-4 py-8 sm:px-6 sm:py-14">
	<!-- Hero Command Search Bar (Concept 2 Core) -->
	<section class="mb-6 sm:mb-8">
		<div class="relative rounded-lg border border-[#30363D] bg-[#161B22] p-2 sm:p-2.5 shadow-2xl transition-all focus-within:border-[#58A6FF] focus-within:shadow-[0_0_20px_rgba(88,166,255,0.15)]">
			<div class="flex items-center gap-2 px-2 sm:px-3 font-mono">
				<span class="text-[#58A6FF] font-bold text-sm select-none">&gt;</span>
				<span class="text-[11px] font-sans font-semibold tracking-wider text-[#8B949E] select-none hidden md:inline">بحث://</span>
				
				<input
					id="search-query-input"
					name="query"
					bind:this={inputEl}
					bind:value={query}
					on:input={handleInput}
					type="text"
					placeholder="ابحث عن اسم المسلسل (مثال: طائر الرفراف، الحفرة)..."
					class="w-full bg-transparent font-sans text-base sm:text-sm text-[#F0F6FC] placeholder-[#8B949E] outline-none border-none focus:ring-0 focus:outline-none py-1.5 sm:py-1"
					dir="rtl"
				/>

				{#if query}
					<button
						on:click={() => { query = ''; searchResults = []; errMsg = ''; }}
						type="button"
						class="min-h-[36px] min-w-[48px] rounded border border-[#30363D] bg-[#21262D] px-2.5 py-1 text-xs font-sans font-medium text-[#8B949E] hover:border-[#F85149] hover:text-[#F85149] active:scale-95 flex items-center justify-center transition"
						title="مسح حقل البحث (ESC)"
					>
						إلغاء
					</button>
				{:else}
					<kbd class="hidden md:inline-block rounded border border-[#30363D] bg-[#21262D] px-1.5 py-0.5 font-mono text-[10px] text-[#8B949E]">
						CTRL+K
					</kbd>
				{/if}
			</div>
		</div>

		<!-- Status & Feedback helper bar -->
		<div class="mt-2 flex items-center justify-between px-1 text-[11px] text-[#8B949E]">
			<div class="flex items-center gap-2">
				{#if loading}
					<span class="inline-block h-2 w-2 animate-ping rounded-full bg-[#58A6FF]"></span>
					<span class="text-[#58A6FF]">[جاري البحث في الفهرس...]</span>
				{:else if query && searchResults.length > 0}
					<span class="text-[#3FB950]">[تم العثور على {searchResults.length} نتيجة]</span>
				{:else if query && errMsg}
					<span class="text-[#F85149]">[{errMsg}]</span>
				{:else}
					<span class="text-[#8B949E]">أداة استخراج روابط المشاهدة المباشرة بدقة عالية</span>
				{/if}
			</div>
		</div>
	</section>

	<!-- Main Content Area: Search Results OR Latest 5 Episodes -->
	{#if query.trim().length > 0}
		<!-- Search Results Grid -->
		<section class="space-y-4">
			<div class="flex items-center justify-between border-b border-[#30363D] pb-2 text-xs">
				<span class="text-[#F0F6FC]">نتائج البحث // {query}</span>
				<span class="text-[#8B949E] font-mono">{searchResults.length} عنوان</span>
			</div>

			{#if loading}
				<div class="rounded-lg border border-[#30363D] bg-[#161B22] p-12 text-center text-xs text-[#8B949E]">
					<div class="inline-block h-6 w-6 animate-spin rounded-full border-2 border-[#58A6FF] border-t-transparent mb-3"></div>
					<div>جاري الاتصال بمحرك البحث وتحديث الفهرس...</div>
				</div>
			{:else if searchResults.length > 0}
				<div class="grid grid-cols-1 gap-3 sm:grid-cols-2 md:grid-cols-3">
					{#each searchResults as item}
						<SearchCard title={item.title} url={`/series/${item.id}`} cover={item.cover} />
					{/each}
				</div>
			{:else if errMsg}
				<div class="rounded-lg border border-[#30363D] bg-[#161B22] p-8 text-center text-xs">
					<div class="text-[#F85149] mb-1 font-mono">[لا توجد نتائج]</div>
					<div class="text-[#8B949E] font-sans">{errMsg}</div>
				</div>
			{/if}
		</section>
	{:else}
		<!-- Final 5 Episodes Retrieved Live from https://qeseh.net/son-bolumler -->
		<section class="rounded-lg border border-[#30363D] bg-[#161B22] shadow-xl overflow-hidden">
			<!-- Terminal Header -->
			<div class="flex flex-wrap items-center justify-between gap-2 border-b border-[#30363D] bg-[#0D1117] px-4 py-3">
				<div class="flex items-center gap-2">
					<span class="h-2 w-2 rounded-full bg-[#2EA043]"></span>
					<h2 class="text-xs font-bold tracking-tight text-[#F0F6FC]">
						قائمة الانتظار // آخر 5 حلقات مضافة
					</h2>
				</div>
				<div class="flex items-center gap-2 text-[10px]">
					<span class="rounded bg-[#21262D] px-2 py-0.5 text-[#3FB950] border border-[#30363D]">
						بث حي • تحديث كل 10 دقائق
					</span>
				</div>
			</div>

			<!-- Episodes Content: Responsive Reflow -->
			{#if latestEpisodes.length === 0}
				<div class="p-8 text-center text-xs text-[#8B949E]">
					<div class="inline-block h-4 w-4 animate-spin rounded-full border-2 border-[#58A6FF] border-t-transparent mb-2"></div>
					<div>جاري جلب أحدث الحلقات من المصدر...</div>
				</div>
			{:else}
				<!-- Mobile & Mid-range Card Feed (< 768px) -->
				<div class="block md:hidden divide-y divide-[#21262D]">
					{#each latestEpisodes as ep}
						<div class="p-3.5 sm:p-4 transition hover:bg-[#21262D]/40">
							<div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
								<!-- Thumbnail & Series Details -->
								<div class="flex items-start sm:items-center gap-3 min-w-0">
									{#if ep.thumb}
										<img
											src={ep.thumb}
											alt={ep.title}
											class="h-14 w-14 sm:h-16 sm:w-16 rounded-md object-cover border border-[#30363D] bg-[#0D1117] flex-shrink-0"
											loading="lazy"
										/>
									{/if}
									<div class="min-w-0">
										<div class="font-sans font-semibold text-[#F0F6FC] text-sm sm:text-base leading-snug line-clamp-2 sm:line-clamp-1">
											{ep.title}
										</div>
										<div class="flex flex-wrap items-center gap-2 mt-1 text-xs">
											<button
												type="button"
												on:click={() => searchSeries(ep.seriesTitle)}
												class="text-[#8B949E] hover:text-[#58A6FF] hover:underline truncate max-w-[170px] text-right"
												title="بحث عن كامل حلقات هذا المسلسل"
											>
												{ep.seriesTitle}
											</button>
											<span class="inline-flex items-center rounded border border-[#30363D] bg-[#0D1117] px-2 py-0.5 font-mono text-[11px] text-[#3FB950] font-bold">
												حلقة {ep.episode}
											</span>
										</div>
									</div>
								</div>

								<!-- Status & Action Buttons -->
								<div class="flex items-center justify-between sm:justify-end gap-2 pt-2 sm:pt-0 border-t sm:border-t-0 border-[#21262D] flex-shrink-0">
									<span class="text-[11px] font-mono {ep.streamLink ? 'text-[#3FB950]' : 'text-[#8B949E]'}">
										{ep.streamLink ? '[جاهز 1080p]' : '[قيد المعالجة]'}
									</span>

									<div class="flex items-center gap-1.5">
										<button
											type="button"
											on:click={() => playEpisode(ep.streamLink || ep.url)}
											class="inline-flex min-h-[38px] items-center gap-1 rounded border border-[#2EA043] bg-[#2EA043] px-3 py-1.5 text-xs font-bold text-white shadow transition hover:bg-[#2c973f] active:scale-95"
											title="فتح الرابط للمشاهدة في علامة تبويب جديدة"
										>
											<svg class="h-3.5 w-3.5" fill="currentColor" viewBox="0 0 24 24">
												<path d="M8 5v14l11-7z"/>
											</svg>
											<span>مشاهدة</span>
										</button>

										<button
											type="button"
											on:click={() => copyStream(ep.streamLink || ep.url, ep.title)}
											class="inline-flex min-h-[38px] items-center gap-1.5 rounded border border-[#30363D] bg-[#21262D] px-3 py-1.5 text-xs font-semibold text-[#58A6FF] transition hover:border-[#58A6FF] hover:bg-[#30363D] hover:text-[#79C0FF] active:scale-95"
											title="نسخ رابط الحلقة"
										>
											<svg class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
												<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 5H6a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2v-1M8 5a2 2 0 002 2h2a2 2 0 002-2M8 5a2 2 0 012-2h2a2 2 0 012 2m0 0h2a2 2 0 012 2v3m2 4H10m0 0l3-3m-3 3l3 3" />
											</svg>
											<span>نسخ</span>
										</button>
									</div>
								</div>
							</div>
						</div>
					{/each}
				</div>

				<!-- Desktop Episodes Table (>= 768px) -->
				<div class="hidden md:block overflow-x-auto">
					<table class="w-full text-right text-xs">
						<thead class="border-b border-[#30363D] bg-[#161B22] text-[11px] text-[#8B949E]">
							<tr>
								<th class="py-2.5 px-4 font-medium">المسلسل والحلقة</th>
								<th class="py-2.5 px-4 font-medium hidden sm:table-cell">رقم الحلقة</th>
								<th class="py-2.5 px-4 font-medium hidden md:table-cell">حالة البث</th>
								<th class="py-2.5 px-4 font-medium text-left">إجراء مباشر</th>
							</tr>
						</thead>
						<tbody class="divide-y divide-[#21262D]">
							{#each latestEpisodes as ep}
								<tr class="transition hover:bg-[#21262D]/60 group">
									<!-- Series Title & Thumbnail -->
									<td class="py-3 px-4">
										<div class="flex items-center gap-3">
											{#if ep.thumb}
												<img
													src={ep.thumb}
													alt={ep.title}
													class="h-10 w-10 sm:h-12 sm:w-12 rounded object-cover border border-[#30363D] bg-[#0D1117] flex-shrink-0"
													loading="lazy"
												/>
											{/if}
											<div>
												<div class="font-sans font-semibold text-[#F0F6FC] group-hover:text-[#58A6FF] transition text-sm">
													{ep.title}
												</div>
												<div class="flex items-center gap-2 mt-0.5 text-[11px] text-[#8B949E]">
													<button
														type="button"
														on:click={() => searchSeries(ep.seriesTitle)}
														class="hover:underline hover:text-[#58A6FF] cursor-pointer"
														title="بحث عن كامل حلقات هذا المسلسل"
													>
														{ep.seriesTitle}
													</button>
													<span class="sm:hidden text-[#3FB950] font-mono">حلقة {ep.episode}</span>
												</div>
											</div>
										</div>
									</td>

									<!-- Episode Tag -->
									<td class="py-3 px-4 hidden sm:table-cell">
										<span class="inline-flex items-center rounded border border-[#30363D] bg-[#0D1117] px-2 py-0.5 font-mono text-[11px] text-[#C9D1D9]">
											حلقة {ep.episode}
										</span>
									</td>

									<!-- Ingest Status -->
									<td class="py-3 px-4 hidden md:table-cell text-[11px]">
										{#if ep.streamLink}
											<span class="text-[#3FB950] font-mono">[جاهز 1080p]</span>
										{:else}
											<span class="text-[#8B949E] font-mono">[قيد المعالجة]</span>
										{/if}
									</td>

									<!-- Action Play & Copy -->
									<td class="py-3 px-4 text-left">
										<div class="flex items-center justify-end gap-1.5">
											<button
												type="button"
												on:click={() => playEpisode(ep.streamLink || ep.url)}
												class="inline-flex items-center gap-1 rounded border border-[#2EA043] bg-[#2EA043] px-2.5 py-1.5 text-[11px] font-bold text-white shadow transition hover:bg-[#2c973f] active:scale-95"
												title="فتح الرابط للمشاهدة في علامة تبويب جديدة"
											>
												<svg class="h-3 w-3" fill="currentColor" viewBox="0 0 24 24">
													<path d="M8 5v14l11-7z"/>
												</svg>
												<span>مشاهدة</span>
											</button>

											<button
												type="button"
												on:click={() => copyStream(ep.streamLink || ep.url, ep.title)}
												class="inline-flex items-center gap-1 rounded border border-[#30363D] bg-[#21262D] px-2.5 py-1.5 text-[11px] font-semibold text-[#58A6FF] transition hover:border-[#58A6FF] hover:bg-[#30363D] hover:text-[#79C0FF] active:scale-95"
												title="نسخ الرابط"
											>
												<svg class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
													<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 5H6a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2v-1M8 5a2 2 0 002 2h2a2 2 0 002-2M8 5a2 2 0 012-2h2a2 2 0 012 2m0 0h2a2 2 0 012 2v3m2 4H10m0 0l3-3m-3 3l3 3" />
												</svg>
												<span>نسخ</span>
											</button>
										</div>
									</td>
								</tr>
							{/each}
						</tbody>
					</table>
				</div>
			{/if}
		</section>
	{/if}
</main>
