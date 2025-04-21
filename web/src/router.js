import { createRouter, createWebHistory } from 'vue-router'  // 从同一个模块导入

const routes = [
    {
        path: '/',
        name: 'index',
        component: () => import('@/components/index.vue')
    },{
        path: '/login',
        name: 'login',
        component:()=>import('@/components/login.vue')
    },
    {
        path: '/register',
        name: 'register',
        component:()=>import('@/components/register.vue')
    },
    {
        path: '/reset_password',
        name: 'reset_password',
        component:()=>import('@/components/reset_password.vue')
    }
]

// 创建并导出路由实例
const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router