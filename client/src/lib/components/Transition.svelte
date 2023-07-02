<script>
	import { fly } from 'svelte/transition'

	/** @type {string} */
	export let url = ''
</script>

{#key url}
	<div in:fly={{ y: -50, duration: 300, delay: 250 }}>
		<slot />
	</div>
{/key}
