<template>
    <div>
        <h1>用户注册</h1>
        <form @submit.prevent="registerUser">
            <div>
                <label for="username">昵称：</label>
                <input type="text" v-model="user.userName" required />
            </div>
            <div>
                <label for="username">账户名：</label>
                <input type="text" v-model="user.userAccount" required />
            </div>
            <div>
                <label for="email">邮箱:</label>
                <input type="email" v-model="user.email" required />
            </div>
            <div>
                <label for="password">密码:</label>
                <input type="password" v-model="user.password" required />
            </div>
            <button type="submit">注册</button>
        </form>
        <p v-if="errorMessage">{{ errorMessage }}</p>
    </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import axios from 'axios';

export default defineComponent({
    name: 'RegisterComponent',
    setup() {
        const user = ref({
            userAccount: '',
            userName: '',
            email: '',
            password: ''
        });
        const errorMessage = ref('');

        const registerUser = async () => {
            try {
                const response = await axios.post('/ff14/user/register', user.value);
                console.log(response.data);
                alert('注册成功！');
            } catch (error) {
                console.error(error);
                errorMessage.value = '注册失败，请重试。';
            }
        };

        return {
            user,
            registerUser,
            errorMessage
        };
    }
});
</script>

<style scoped>
/* 添加样式 */
</style>