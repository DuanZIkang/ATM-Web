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

// ✅ 响应拦截：统一处理错误响应，提取后端自定义错误消息
request.interceptors.response.use(
    res => res,
    err => {
        // 401 自动清除登录态并跳回登录页
        if (err.response?.status === 401) {
            sessionStorage.clear();
            router.push("/login");
        }

        // 将后端返回的错误消息附加到错误对象上，方便业务代码使用
        if (err.response?.data?.message) {
            err.message = err.response.data.message;
        }

        return Promise.reject(err);
    }
);

export default request;