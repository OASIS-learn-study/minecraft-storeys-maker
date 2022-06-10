import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      "/login/": {
        target: "http://localhost:7070"
      },
      "/code/": {
        target: "http://localhost:7070"
      }
    }
  }
})
