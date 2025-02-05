import axios from 'axios';
import store from '@/store';
import router from '@/router';

const instance = axios.create({
    baseURL: process.env.VUE_APP_API_URL,
    timeout: 100000,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json'
    }
});

// 请求拦截器
instance.interceptors.request.use(
    config => {
        const token = store.state.auth.token;
        if (token && config.headers) {
            try {
                // 添加 token 到请求头
                config.headers.Authorization = `Bearer ${token}`;
            } catch (error) {
                console.error('设置请求头失败:', error);
            }
        }
        // 确保 API 请求路径正确
        if (config.url && !config.url.startsWith('http')) {
            config.url = `${process.env.VUE_APP_API_URL}${config.url}`;
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// 响应拦截器
instance.interceptors.response.use(
    response => {
        // 打印响应数据，用于调试
        console.log('Response:', response);
        return response;
    },
    error => {
        console.error('Request error:', error);
        if (error.response?.status === 401) {
            store.commit('auth/logout');
            router.push('/login');
        }
        return Promise.reject(error);
    }
);

export default instance;
