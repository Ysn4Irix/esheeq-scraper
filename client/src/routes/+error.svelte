<script>
	import { page } from '$app/stores'
</script>

<svelte:head>
	<title>خطأ {$page.status} // قصة عشق</title>
</svelte:head>

<div class="flex min-h-[80vh] flex-col items-center justify-center p-6 text-center">
	<div class="w-full max-w-md rounded-lg border border-[#30363D] bg-[#161B22] p-8 shadow-2xl">
		<div class="mb-3 text-xs font-bold text-[#F85149] font-mono">[خطأ في النظام: رمز {$page.status}]</div>
		<h1 class="text-6xl font-black text-[#F0F6FC] tracking-tighter font-mono">
			{$page.status === 404 ? '404' : '500'}
		</h1>
		<div class="my-4 border-t border-[#30363D] pt-3 text-xs text-[#8B949E]">
			{#if $page.status === 404}
				<p>لم يتم العثور على المسار أو المسلسل المطلوب في الأرشيف.</p>
			{:else}
				<p>حدث خطأ غير متوقع في الخادم أثناء معالجة الطلب.</p>
			{/if}
		</div>
		<a
			href="/"
			class="mt-3 inline-flex items-center gap-2 rounded border border-[#30363D] bg-[#21262D] px-4 py-2 text-xs font-semibold text-[#58A6FF] transition hover:border-[#58A6FF] hover:text-[#79C0FF]"
		>
			<span>الرجوع للصفحة الرئيسية ←</span>
		</a>
	</div>
</div>
