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
			colors: {
				terminal: {
					bg: '#0D1117',
					panel: '#161B22',
					sub: '#21262D',
					border: '#30363D',
					blue: '#58A6FF',
					green: '#2EA043',
					emerald: '#3FB950',
					muted: '#8B949E',
					text: '#F0F6FC'
				}
			},
			fontFamily: {
				mono: ['JetBrains Mono', 'monospace'],
				sans: ['Tajawal', 'IBM Plex Sans Arabic', 'system-ui', 'sans-serif'],
				tajawal: ['Tajawal', 'sans-serif'],
				ibx: ['IBM Plex Sans Arabic', 'sans-serif']
			}
		}
	},
	plugins: [require('@tailwindcss/forms'), require('preline/plugin')]
}

module.exports = config
