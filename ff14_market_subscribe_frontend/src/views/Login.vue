<template>
    <div class="login-container">
        <div class="nav-bar">
            <router-link to="/" class="nav-item">首页</router-link>
            <router-link to="/subscribe" class="nav-item">物品订阅</router-link>
            <router-link to="/realtime" class="nav-item">实时物价</router-link>
            <router-link to="/itemsearch" class="nav-item">物价查询</router-link>
        </div>
        <div class="content">
            <div class="login-box">
                <h1 class="title">登录</h1>
                <form @submit.prevent="handleLogin" class="login-form">
                    <div class="form-group">
                        <input type="text" v-model="loginForm.userAccount" class="input-field" placeholder="请输入账户名/邮箱"
                            required>
                    </div>
                    <div class="form-group">
                        <input type="password" v-model="loginForm.password" class="input-field" placeholder="请输入密码"
                            required>
                    </div>
                    <div class="form-group">
                        <button type="submit" class="login-btn">登录</button>
                    </div>
                    <div class="form-footer">
                        <router-link to="/register" class="register-link">没有账号？立即注册</router-link>
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import axios from '@/utils/axios';

export default defineComponent({
    name: 'LoginPage',
    setup() {
        const router = useRouter();
        const store = useStore();
        const loginForm = ref({
            userAccount: '',
            password: ''
        });

        const handleLogin = async () => {
            try {
                // 1. 登录请求
                const loginResponse = await axios.post('/ff14/user/login', loginForm.value);
                console.log('Login response:', loginResponse.data);

                // 2. 设置 token
                const { tokenValue } = loginResponse.data.data;
                if (!tokenValue) {
                    throw new Error('登录返回的数据格式不正确');
                }

                // 3. 更新 Vuex store
                store.commit('auth/setToken', tokenValue);
                store.commit('auth/setLoggedIn', true);

                // 4. 获取用户信息
                const userInfoResponse = await axios.get(`/ff14/user/info`);
                console.log('User info response:', userInfoResponse.data);

                const userInfo = userInfoResponse.data.data;
                if (userInfo) {
                    store.commit('auth/setUser', {
                        userName: userInfo.userName,
                        email: userInfo.email
                    });
                }

                // 5. 跳转到首页
                router.push('/');
            } catch (error: any) {
                console.error('登录失败:', error);
                const errorMessage = error.response?.data?.message || error.response?.data?.data || '登录失败，请重试';
                alert(errorMessage);
            }
        };

        return {
            loginForm,
            handleLogin
        };
    }
});
</script>

<style scoped>
.login-container {
    min-height: 100vh;
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
    position: relative;
}

.content {
    display: flex;
    justify-content: center;
    align-items: center;
    padding-top: 100px;
}

.login-box {
    width: 400px;
    padding: 40px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.title {
    font-size: 24px;
    color: #333;
    text-align: center;
    margin-bottom: 30px;
    font-weight: normal;
}

.login-form {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.form-group {
    display: flex;
    flex-direction: column;
}

.input-field {
    height: 40px;
    padding: 0 15px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 14px;
    transition: border-color 0.3s;
}

.input-field:focus {
    outline: none;
    border-color: #4285f4;
}

.login-btn {
    width: 100%;
    height: 40px;
    background-color: #4285f4;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 14px;
    cursor: pointer;
    transition: background-color 0.3s;
}

.login-btn:hover {
    background-color: #3367d6;
}

.form-footer {
    text-align: center;
    margin-top: 10px;
}

.register-link {
    color: #4285f4;
    text-decoration: none;
    font-size: 14px;
}

.register-link:hover {
    text-decoration: underline;
}
</style>
