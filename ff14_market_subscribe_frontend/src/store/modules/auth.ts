import { Module } from 'vuex';
import { RootState, AuthState } from '../types';
import axios from '@/utils/axios';

const auth: Module<AuthState, RootState> = {
    namespaced: true,
    state: {
        token: localStorage.getItem('token'),
        isLoggedIn: !!localStorage.getItem('token'),
        username: localStorage.getItem('username'),
        email: localStorage.getItem('email')
    },
    mutations: {
        setToken(state, token: string) {
            try {
                // 确保 token 是字符串
                if (typeof token !== 'string') {
                    console.error('Invalid token type:', typeof token);
                    return;
                }
                
                state.token = token;
                localStorage.setItem('token', token);
                
                // 设置 axios 默认 headers
                axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
            } catch (error) {
                console.error('设置 token 失败:', error);
            }
        },
        setUser(state, user: { userName: string; email: string }) {
            if (user) {
                state.username = user.userName;
                state.email = user.email;
                localStorage.setItem('username', user.userName);
                localStorage.setItem('email', user.email);
            }
        },
        setLoggedIn(state, value: boolean) {
            state.isLoggedIn = value;
        },
        logout(state) {
            state.token = null;
            state.isLoggedIn = false;
            state.username = null;
            state.email = null;
            localStorage.removeItem('token');
            localStorage.removeItem('username');
            localStorage.removeItem('email');
            // 清除 axios 默认 headers
            delete axios.defaults.headers.common['Authorization'];
        }
    }
};

export default auth;
