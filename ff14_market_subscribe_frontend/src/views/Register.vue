<template>
    <div class="register-container">
        <div class="nav-bar">
            <router-link to="/" class="nav-item">首页</router-link>
            <router-link to="/subscribe" class="nav-item">物品订阅</router-link>
            <router-link to="/realtime" class="nav-item">实时物价</router-link>
        </div>
        <div class="content">
            <div class="register-box">
                <h1 class="title">用户注册</h1>
                <form @submit.prevent="handleRegister" class="register-form">
                    <div class="form-group">
                        <input type="text" v-model="user.userName" class="input-field" placeholder="请输入昵称" required>
                    </div>
                    <div class="form-group">
                        <input type="text" v-model="user.userAccount" class="input-field" placeholder="请输入账户名" required>
                    </div>
                    <div class="form-group">
                        <input type="email" v-model="user.email" class="input-field" placeholder="请输入邮箱" required>
                    </div>
                    <div class="form-group">
                        <input type="password" v-model="user.password" class="input-field" placeholder="请输入密码" required
                            minlength="6" maxlength="20">
                    </div>
                    <div class="form-group">
                        <input type="password" v-model="confirmPassword" class="input-field" placeholder="请确认密码"
                            required minlength="6" maxlength="20">
                    </div>
                    <div v-if="passwordError" class="error-message">
                        {{ passwordError }}
                    </div>
                    <div class="form-group">
                        <button type="submit" class="register-btn">注册</button>
                    </div>
                    <div class="form-footer">
                        <router-link to="/login" class="login-link">已有账号？立即登录</router-link>
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import axios from '@/utils/axios';

export default defineComponent({
    name: 'RegisterPage',
    setup() {
        const router = useRouter();
        const user = ref({
            userAccount: '',
            userName: '',
            email: '',
            password: ''
        });
        const confirmPassword = ref('');
        const passwordError = computed(() => {
            if (!confirmPassword.value) return '';
            if (user.value.password.length < 6) {
                return '密码长度不能小于6位';
            }
            if (user.value.password !== confirmPassword.value) {
                return '两次输入的密码不一致';
            }
            return '';
        });

        const handleRegister = async () => {
            if (passwordError.value) {
                alert(passwordError.value);
                return;
            }

            try {
                const response = await axios.post('/ff14/user/register', user.value);
                alert(response.data.message || '注册成功！');
                router.push('/login');
            } catch (error: any) {
                console.error('注册失败:', error);
                alert(error.response?.data?.message || error.response?.data?.data || '注册失败，请重试');
            }
        };

        return {
            user,
            confirmPassword,
            passwordError,
            handleRegister
        };
    }
});
</script>

<style scoped>
.register-container {
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

.register-box {
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

.register-form {
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

.register-btn {
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

.register-btn:hover {
    background-color: #3367d6;
}

.form-footer {
    text-align: center;
    margin-top: 10px;
}

.login-link {
    color: #4285f4;
    text-decoration: none;
    font-size: 14px;
}

.login-link:hover {
    text-decoration: underline;
}

.error-message {
    color: #ff4444;
    font-size: 12px;
    margin-top: -10px;
    margin-bottom: 10px;
    text-align: left;
    padding: 0 2px;
}
</style>