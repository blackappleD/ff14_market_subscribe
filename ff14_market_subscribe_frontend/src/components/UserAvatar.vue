<template>
    <div class="user-avatar">
        <div v-if="!isLoggedIn" class="avatar" @click="openLoginModal">
            <i class="fas fa-user"></i>
        </div>
        <div v-else class="avatar" @click="toggleUserMenu">
            <span class="username-initial">{{ usernameInitial }}</span>
            <div v-if="showMenu" class="user-menu">
                <span class="username">{{ username }}</span>
                <div class="menu-item" @click="handleLogout">注销</div>
            </div>
        </div>

        <!-- 登录弹窗 -->
        <div v-if="showLoginModal" class="modal-overlay" @click="closeLoginModal">
            <div class="modal-content" @click.stop>
                <h2>用户登录</h2>
                <form @submit.prevent="handleLogin">
                    <div class="form-group">
                        <label>用户名/邮箱:</label>
                        <input v-model="loginForm.account" type="text" required />
                    </div>
                    <div class="form-group">
                        <label>密码:</label>
                        <input v-model="loginForm.password" type="password" required />
                    </div>
                    <div class="error-message" v-if="errorMessage">{{ errorMessage }}</div>
                    <button type="submit">登录</button>
                </form>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue';
import { useStore } from 'vuex';
import axios from '@/utils/axios';
import { RootState } from '@/store/types';

export default defineComponent({
    name: 'UserAvatar',
    setup() {
        const store = useStore<RootState>();
        const showLoginModal = ref(false);
        const showMenu = ref(false);
        const errorMessage = ref('');
        const loginForm = ref({
            account: '',
            password: ''
        });

        const isLoggedIn = computed(() => store.state.auth.isLoggedIn);
        const username = computed(() => store.state.auth.username);
        const usernameInitial = computed(() => {
            return username.value ? username.value.charAt(0).toUpperCase() : '';
        });

        const openLoginModal = () => {
            showLoginModal.value = true;
        };

        const closeLoginModal = () => {
            showLoginModal.value = false;
            errorMessage.value = '';
            loginForm.value = { account: '', password: '' };
        };

        const toggleUserMenu = () => {
            showMenu.value = !showMenu.value;
        };

        const handleLogin = async () => {
            try {
                const response = await axios.post('/ff14/user/login', loginForm.value);
                store.commit('auth/setAuth', {
                    token: response.data.token,
                    username: response.data.username
                });
                closeLoginModal();
            } catch (error) {
                console.error(error);
                errorMessage.value = '登录失败，请检查用户名和密码';
            }
        };

        const handleLogout = async () => {
            try {
                await axios.post('/ff14/user/logout');
                store.commit('auth/clearAuth');
                showMenu.value = false;
            } catch (error) {
                console.error('注销失败:', error);
            }
        };

        return {
            showLoginModal,
            showMenu,
            loginForm,
            errorMessage,
            isLoggedIn,
            username,
            usernameInitial,
            openLoginModal,
            closeLoginModal,
            toggleUserMenu,
            handleLogin,
            handleLogout
        };
    }
});
</script>

<style scoped>
.user-avatar {
    position: fixed;
    top: 20px;
    right: 20px;
    z-index: 1000;
}

.avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background-color: #42b983;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    color: white;
    font-weight: bold;
}

.username-initial {
    font-size: 18px;
}

.user-menu {
    position: absolute;
    top: 50px;
    right: 0;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    padding: 10px 0;
    min-width: 150px;
}

.username {
    display: block;
    padding: 10px 20px;
    border-bottom: 1px solid #eee;
    color: #666;
}

.menu-item {
    padding: 10px 20px;
    cursor: pointer;
    color: #333;
}

.menu-item:hover {
    background-color: #f5f5f5;
}

.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1001;
}

.modal-content {
    background: white;
    padding: 30px;
    border-radius: 8px;
    width: 300px;
}

.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    margin-bottom: 8px;
    color: #333;
}

.form-group input {
    width: 100%;
    padding: 8px;
    border: 1px solid #ddd;
    border-radius: 4px;
}

.error-message {
    color: #ff4444;
    margin-bottom: 10px;
    font-size: 14px;
}

button {
    width: 100%;
    padding: 10px;
    background-color: #42b983;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}

button:hover {
    background-color: #36a76a;
}
</style>