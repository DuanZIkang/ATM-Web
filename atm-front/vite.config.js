import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
    plugins: [vue()],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src')
        }
    },
    optimizeDeps: {
        include: ['crypto-js']  // 预优化 crypto-js，避免 Rollup 解析失败
    },
    server: {
        proxy: {
            // 只要请求路径以 /api 开头
            '/api': {
                target: 'http://localhost:8090', // 转发给后端真实地址
                changeOrigin: true,             // 允许跨域
                // 如果后端接口没有 /api 前缀，可以用 rewrite 去掉它
                // rewrite: (path) => path.replace(/^\/api/, '')
            }
        }
    }
})