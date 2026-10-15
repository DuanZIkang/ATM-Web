import { createApp } from 'vue'
import App from './App.vue'
import '@/assets/styles/global.css'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// ✅ 引入 router
import router from './router'
import axios from "axios";

const app = createApp(App)

// ✅ 使用 router
app.use(router)
app.use(ElementPlus)
app.mount('#app')
axios.interceptors.response.use(
    res => res,
    err => {
        if (err.response?.status === 401) {
            sessionStorage.clear();
            router.push("/login");
        }
        return Promise.reject(err);
    }
);
