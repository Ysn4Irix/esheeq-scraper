/** @type {import('tailwindcss').Config}*/
const config = {
	darkMode: 'class',
	content: ['./src/**/*.{html,js,svelte,ts}', 'node_modules/preline/dist/*.js'],

	theme: {
		screens: {
			xs: '320px',
			sm: '480px',
			md: '768px',
			lg: '976px',
			xl: '1440px'
		},
		extend: {
			fontFamily: {
				varela: ['Varela Round', 'sans-serif'],
				pacifico: ['Pacifico', 'sans-serif'],
				ibx: ['IBM Plex Sans Arabic', 'sans-serif']
			}
		}
	},
	plugins: [require('@tailwindcss/forms'), require('preline/plugin')]
}

module.exports = config
