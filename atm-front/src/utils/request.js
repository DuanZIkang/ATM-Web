import axios from "axios";
const BASE_URL = "";
const service = axios.create({
    baseURL:  BASE_URL + "/api/atm", // 后端接口地址（端口你刚才改成8090了）
    timeout: 5000
});

// 请求拦截器（可选）
service.interceptors.request.use(config => {
    return config;
}, error => {
    return Promise.reject(error);
});

// 响应拦截器（可选）
service.interceptors.response.use(response => {
    return response.data;
}, error => {
    return Promise.reject(error);
});

export default service;
