<template>
    <div class="home-container">
        <div class="nav-bar">
            <router-link to="/" class="nav-item active">首页</router-link>
            <router-link to="/subscribe" class="nav-item">物品订阅</router-link>
            <router-link to="/realtime" class="nav-item">实时物价</router-link>
        </div>
        <div class="content">
            <template v-if="!isLoggedIn">
                <h1 class="title">首页</h1>
                <div class="button-group">
                    <button class="login-btn" @click="goToLogin">登录</button>
                    <button class="register-btn" @click="goToRegister">注册</button>
                </div>
            </template>
            <template v-else>
                <div class="user-profile">
                    <div class="avatar">
                        <span class="avatar-text">头像</span>
                    </div>
                    <div class="user-info">
                        <h2 class="username">{{ username }}</h2>
                        <p class="email">{{ email }}</p>
                        <button class="logout-btn" @click="handleLogout">退出登录</button>
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { RootState } from '@/store/types';
import axios from '@/utils/axios';

export default defineComponent({
    name: 'HomePage',
    setup() {
        const router = useRouter();
        const store = useStore<RootState>();

        const isLoggedIn = computed(() => store.state.auth.isLoggedIn);
        const username = computed(() => store.state.auth.username);
        const email = computed(() => store.state.auth.email);

        const goToLogin = () => {
            router.push('/login');
        };

        const goToRegister = () => {
            router.push('/register');
        };

        const handleLogout = async () => {
            try {
                // 调用后端退出登录接口
                await axios.post('/ff14/user/logout');

                // 清除本地存储的登录状态
                store.commit('auth/logout');

                // 清除 axios 请求头中的 token
                delete axios.defaults.headers.common['Authorization'];

                // 跳转到登录页面
                router.push('/login');
            } catch (error) {
                console.error('退出登录失败:', error);
            }
        };

        return {
            isLoggedIn,
            username,
            email,
            goToLogin,
            goToRegister,
            handleLogout
        };
    }
});
</script>

<style scoped>
.home-container {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background-color: #ffffff;
}

.nav-bar {
    padding: 15px 0;
    display: flex;
    gap: 30px;
    border-bottom: 1px solid #eee;
    margin-left: 20px;
}

.nav-item {
    text-decoration: none;
    color: #666;
    font-size: 14px;
    padding: 0;
    transition: all 0.3s ease;
    position: relative;
}

.nav-item:hover {
    color: #4285f4;
}

.nav-item.active {
    color: #4285f4;
    font-weight: normal;
}

.nav-item.active::after {
    content: '';
    position: absolute;
    bottom: -16px;
    left: 0;
    width: 100%;
    height: 2px;
    background-color: #4285f4;
}

.content {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    margin-top: -100px;
}

.title {
    font-size: 36px;
    color: #333;
    margin-bottom: 60px;
    font-weight: normal;
}

.button-group {
    display: flex;
    gap: 30px;
}

.login-btn,
.register-btn {
    padding: 12px 50px;
    font-size: 14px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s ease;
    min-width: 120px;
}

.login-btn {
    background-color: #4285f4;
    color: white;
}

.login-btn:hover {
    background-color: #3367d6;
}

.register-btn {
    background-color: #e0e0e0;
    color: #333;
}

.register-btn:hover {
    background-color: #d0d0d0;
}

.user-profile {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
}

.avatar {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    background-color: #e0e0e0;
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 20px;
}

.avatar-text {
    color: #666;
    font-size: 14px;
}

.user-info {
    text-align: center;
}

.username {
    font-size: 24px;
    color: #333;
    margin-bottom: 10px;
    font-weight: normal;
}

.email {
    font-size: 14px;
    color: #666;
}

.logout-btn {
    margin-top: 20px;
    padding: 8px 24px;
    background-color: #f44336;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    transition: background-color 0.3s;
}

.logout-btn:hover {
    background-color: #d32f2f;
}

.user-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
}
</style>