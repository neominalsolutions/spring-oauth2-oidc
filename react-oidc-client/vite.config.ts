import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';

// https://vite.dev/config/
export default defineConfig({
	plugins: [react()],
	server: {
		proxy: {
			'/.well-known': 'http://localhost:8080/.well-known/openid-configuration',
			'/oauth2': 'http://localhost:8080',
		},
	},
});
