import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0',
    port: 5173,
    watch: {
      usePolling: true,
    },
    hmr: {
      host: '127.0.0.1',
      port: 5173,
    },
    proxy: {
      // Proxy API requests to the backend service inside the compose network
      '/api': {
        target: 'http://backend:8081',
        changeOrigin: true,
        secure: false,
      },
    },
  },
})
