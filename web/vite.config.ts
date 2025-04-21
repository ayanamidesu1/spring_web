import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import VueDevTools from 'vite-plugin-vue-devtools';
import VueSetupExtend from 'vite-plugin-vue-setup-extend';
import mkcert from 'vite-plugin-mkcert';
import path from 'path';
import fs from 'fs'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(),
     VueDevTools(),
    VueSetupExtend(),
    //mkcert(),
  ],
  server: {
    port: 3000,
    https: {
      key: fs.readFileSync(path.resolve(__dirname, 'web.key')),
      cert: fs.readFileSync(path.resolve(__dirname, 'web.pem'))
    },
    cors: false // 完全禁用开发服务器CORS中间件
  },
  resolve: {
    alias: {
      '@assets': path.resolve(process.cwd(), 'src/assets'),  // 将 @assets 映射到 src/assets
      '@': path.resolve(process.cwd(), 'src')  // 可选：同时配置 @ 别名指向 src 根目录
    },
  }
})
