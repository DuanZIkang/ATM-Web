import axios from "axios";
import router from "@/router";

const request = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
});

// ✅ 请求拦截：自动注入 token，不用每个页面手动写
request.interceptors.request.use(config => {
    const token = sessionStorage.getItem("token");
    if (token) {
        config.headers["token"] = token;
    }
    return config;
});

// ✅ 响应拦截：401 自动清除登录态并跳回登录页
request.interceptors.response.use(
    res => res,
    err => {
        if (err.response?.status === 401) {
            sessionStorage.clear();
            router.push("/login");
        }
        return Promise.reject(err);
    }
);

export default request;