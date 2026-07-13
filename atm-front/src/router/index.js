import { createRouter, createWebHashHistory } from 'vue-router'

import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import Info from '../views/Info.vue'
import Deposit from '../views/Deposit.vue'
import Withdraw from '../views/Withdraw.vue'
import Transfer from '../views/Transfer.vue'
import ChangePassword from "@/views/ChangePassword.vue";
import TransactionRecords from "@/views/TransactionRecords.vue";

const routes = [
    {
        path: '/',
        component: Login,
        meta: { title: "登录 " }
    },
    {
        path: '/login',
        component: Login,
        meta: { title: "登录 " }
    },
    {
        path: '/register',
        component: Register,
        meta: { title: "开户 " }
    },
    {
        path: '/home',
        component: Home,
        meta: { title: "首页 " }
    },
    {
        path: '/info',
        component: Info,
        meta: { title: "账户信息 " }
    },
    {
        path: '/deposit',
        component: Deposit,
        meta: { title: "存款 " }
    },
    {
        path: '/withdraw',
        component: Withdraw,
        meta: { title: "取款 " }
    },
    {
        path: '/transfer',
        component: Transfer,
        meta: { title: "转账 " }
    },
    {
        path: '/change-password',
        component: ChangePassword,
        meta: { title: "修改密码 " }
    },
    {
        path: '/transaction-records',
        component: TransactionRecords,
        meta: { title: "交易记录 " }
    }
]

const router = createRouter({
    history: createWebHashHistory(),
    routes
})

// 自动切换页面标题
router.beforeEach((to, from, next) => {
    if (to.meta.title) {
        document.title = to.meta.title
    }
    next()
})

export default router
