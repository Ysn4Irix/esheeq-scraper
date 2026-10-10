<script>
	import { onMount } from 'svelte'

	/** @type {boolean} */
	export let arrowBack = false

	let isOpen = false
	let currentTheme = 'dark'

	const themes = [
		{ id: 'dark', label: 'الوضع الداكن' },
		{ id: 'light', label: 'الوضع الفاتح' },
		{ id: 'auto', label: 'تلقائي (حسب النظام)' }
	]

	onMount(() => {
		currentTheme = localStorage.getItem('theme') || 'dark'
		applyTheme(currentTheme)

		const handleOutsideClick = (e) => {
			const container = document.getElementById('theme-dropdown-container')
			if (container && !container.contains(e.target)) {
				isOpen = false
			}
		}

		const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
		const handleSystemThemeChange = (e) => {
			if (currentTheme === 'auto') {
				if (e.matches) {
					document.documentElement.classList.add('dark')
				} else {
					document.documentElement.classList.remove('dark')
				}
			}
		}

		mediaQuery.addEventListener('change', handleSystemThemeChange)
		window.addEventListener('click', handleOutsideClick)

		return () => {
			window.removeEventListener('click', handleOutsideClick)
			mediaQuery.removeEventListener('change', handleSystemThemeChange)
		}
	})

	function applyTheme(theme) {
		currentTheme = theme
		localStorage.setItem('theme', theme)
		const html = document.documentElement

		if (theme === 'dark') {
			html.classList.add('dark')
		} else if (theme === 'light') {
			html.classList.remove('dark')
		} else if (theme === 'auto') {
			const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
			if (prefersDark) {
				html.classList.add('dark')
			} else {
				html.classList.remove('dark')
			}
		}
	}

	function setTheme(theme) {
		applyTheme(theme)
		isOpen = false
	}
</script>

<header class="sticky top-0 z-50 border-b border-[#30363D] bg-[#161B22]/95 backdrop-blur-md px-3.5 py-2 sm:px-6">
	<div class="mx-auto flex max-w-6xl items-center justify-between gap-3 text-xs">
		<!-- Right (Start in RTL): Brand & Return link -->
		<div class="flex items-center gap-2 sm:gap-3 flex-shrink-0">
			{#if arrowBack}
				<a
					data-sveltekit-preload-data="hover"
					href="/"
					class="inline-flex min-h-[38px] sm:min-h-[32px] items-center gap-1.5 whitespace-nowrap rounded border border-[#30363D] bg-[#21262D] px-2.5 py-1 text-xs text-[#58A6FF] transition hover:border-[#58A6FF] hover:text-[#79C0FF] active:scale-95"
				>
					<span class="font-bold">→</span>
					<span class="hidden xs:inline">العودة للبحث</span>
				</a>
			{/if}

			<a href="/" class="flex items-center gap-1.5 sm:gap-2 whitespace-nowrap text-xs sm:text-sm font-bold tracking-tight text-[#58A6FF] hover:text-[#79C0FF]">
				<span class="text-xs text-[#8B949E]">&gt;</span>
				<span>قصة عشق</span>
				<span class="rounded bg-[#21262D] px-1.5 py-0.5 font-mono text-[9px] sm:text-[10px] font-semibold text-[#8B949E] border border-[#30363D]">V2.0</span>
			</a>
		</div>

		<!-- Left (End in RTL): Theme Switcher with Radio Button UI -->
		<div class="flex items-center gap-2 sm:gap-4 text-[#8B949E]">
			<div id="theme-dropdown-container" class="relative inline-flex">
				<button
					id="hs-dropdown-theme"
					type="button"
					on:click|stopPropagation={() => (isOpen = !isOpen)}
					class="inline-flex min-h-[38px] sm:min-h-[32px] items-center gap-1.5 rounded border border-[#30363D] bg-[#21262D] px-2.5 py-1 text-xs text-[#C9D1D9] transition hover:border-[#58A6FF] active:scale-95"
					aria-expanded={isOpen}
					aria-haspopup="true"
					aria-label="قائمة اختيار المظهر"
				>
					{#if currentTheme === 'light'}
						<svg class="h-3.5 w-3.5 text-[#0969DA] dark:text-[#58A6FF]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
						</svg>
					{:else if currentTheme === 'dark'}
						<svg class="h-3.5 w-3.5 text-[#58A6FF]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
						</svg>
					{:else}
						<svg class="h-3.5 w-3.5 text-[#58A6FF]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
						</svg>
					{/if}
					<span class="hidden sm:inline">المظهر</span>
					<svg class="h-3 w-3 text-[#8B949E] transition-transform duration-150 {isOpen ? 'rotate-180' : ''}" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
					</svg>
				</button>

				{#if isOpen}
					<div
						class="absolute left-0 top-full z-50 mt-1.5 min-w-[175px] rounded-lg border border-[#30363D] bg-[#161B22] p-1.5 shadow-2xl"
						role="radiogroup"
						aria-label="اختيار مظهر الواجهة"
					>
						{#each themes as theme}
							<button
								type="button"
								role="radio"
								aria-checked={currentTheme === theme.id}
								on:click={() => setTheme(theme.id)}
								class="flex w-full items-center justify-between rounded-md px-2.5 py-2 text-right text-xs transition min-h-[38px] {currentTheme === theme.id ? 'bg-[#21262D] text-[#58A6FF] font-semibold' : 'text-[#C9D1D9] hover:bg-[#21262D]/70'}"
							>
								<div class="flex items-center gap-2">
									{#if theme.id === 'dark'}
										<svg class="h-3.5 w-3.5 text-[#58A6FF]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
											<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
										</svg>
									{:else if theme.id === 'light'}
										<svg class="h-3.5 w-3.5 text-[#E3B341]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
											<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
										</svg>
									{:else}
										<svg class="h-3.5 w-3.5 text-[#8B949E]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
											<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
										</svg>
									{/if}
									<span>{theme.label}</span>
								</div>

								<!-- Styled Custom Radio Control -->
								<div class="relative flex h-4 w-4 items-center justify-center rounded-full border transition-all {currentTheme === theme.id ? 'border-[#58A6FF] bg-[#58A6FF]/15' : 'border-[#30363D] bg-transparent'}">
									{#if currentTheme === theme.id}
										<span class="h-2 w-2 rounded-full bg-[#58A6FF]"></span>
									{/if}
								</div>
							</button>
						{/each}
					</div>
				{/if}
			</div>
		</div>
	</div>
</header>
