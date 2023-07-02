<script>
	import '../app.postcss'
	import { page } from '$app/stores'
	import Transition from '$lib/components/Transition.svelte'
</script>

<Transition url={$page.url}>
	<slot />
</Transition>
