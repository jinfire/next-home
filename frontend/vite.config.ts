import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import { loadEnv } from 'vite'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '..', '')
  const backendTarget = process.env.BACKEND_PROXY_TARGET ?? env.BACKEND_PROXY_TARGET ?? 'http://localhost:8080'
  return {
    plugins: [react()],
    define: {
      'import.meta.env.VITE_NAVER_MAP_CLIENT_ID': JSON.stringify(env.NAVER_MAP_CLIENT_ID ?? ''),
    },
    server: {
      proxy: { '/api': backendTarget },
    },
    test: {
      environment: 'jsdom',
      globals: true,
      setupFiles: './src/test/setup.ts',
      exclude: ['e2e/**', 'node_modules/**', 'dist/**'],
    },
  }
})
