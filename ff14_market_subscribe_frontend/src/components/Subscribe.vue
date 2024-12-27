<template>
    <main class="subscribe-container" @click="hideDropdowns">
        <section>
            <h1>订阅物品</h1>
            <form @submit.prevent="subscribeItem">
                <div class="input-group">
                    <label for="worldName">大区:</label>
                    <input type="text" v-model="subscription.worldName" @input="fetchWorlds(subscription.worldName)"
                        placeholder="输入大区名称" required />
                    <ul v-if="worlds.length > 0" class="dropdown-list">
                        <li v-for="world in worlds" :key="world.id" @click="selectWorld(world.name)">
                            {{ world.name }}
                        </li>
                    </ul>
                </div>
                <div class="input-group">
                    <label for="itemName">物品:</label>
                    <input type="text" v-model="subscription.itemName" @input="fetchItems(subscription.itemName)"
                        placeholder="输入物品名称" required />
                    <ul v-if="items.length > 0" class="dropdown-list">
                        <li v-for="item in items" :key="item.id" @click="selectItem(item.name)">
                            {{ item.name }}
                        </li>
                    </ul>
                </div>
                <div class="input-group">
                    <label for="notifyThreshold">价格阈值:</label>
                    <input type="number" v-model="subscription.notifyThreshold" />
                </div>
                <button type="submit">订阅</button>
            </form>
            <p v-if="errorMessage">{{ errorMessage }}</p>
        </section>
    </main>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import axios from 'axios';

interface World {
    id: number;
    name: string;
}

interface Item {
    id: number;
    name: string;
}

export default defineComponent({
    name: 'SubscribeComponent',
    setup() {
        const subscription = ref({
            itemName: '',
            worldName: '',
            notifyThreshold: null
        });
        const errorMessage = ref('');
        const worlds = ref<World[]>([]);
        const items = ref<Item[]>([]);

        let timeout: ReturnType<typeof setTimeout>;

        const fetchWorlds = async (name: string) => {
            clearTimeout(timeout);
            timeout = setTimeout(async () => {
                if (!name) {
                    worlds.value = [];
                    return;
                }
                try {
                    const response = await axios.get(`/ff14/world/search?name=${name}`);
                    worlds.value = response.data;
                } catch (error) {
                    console.error(error);
                    errorMessage.value = '获取大区列表失败，请重试。';
                }
            }, 300);
        };

        const fetchItems = async (name: string) => {
            clearTimeout(timeout);
            timeout = setTimeout(async () => {
                if (!name) {
                    items.value = [];
                    return;
                }
                try {
                    const response = await axios.get(`/ff14/item/search?name=${name}`);
                    items.value = response.data;
                } catch (error) {
                    console.error(error);
                    errorMessage.value = '获取物品列表失败，请重试。';
                }
            }, 300);
        };

        const selectWorld = (name: string) => {
            subscription.value.worldName = name;
            worlds.value = [];
        };

        const selectItem = (name: string) => {
            subscription.value.itemName = name;
            items.value = [];
        };

        const subscribeItem = async () => {
            try {
                const response = await axios.post('/ff14/subscribe', subscription.value);
                console.log(response.data);
                alert('订阅成功！');
            } catch (error) {
                console.error(error);
                errorMessage.value = '订阅失败，请重试。';
            }
        };

        const hideDropdowns = () => {
            worlds.value = [];
            items.value = [];
        };

        return {
            subscription,
            subscribeItem,
            errorMessage,
            worlds,
            items,
            fetchWorlds,
            fetchItems,
            selectWorld,
            selectItem,
            hideDropdowns
        };
    }
});
</script>

<style scoped>
.subscribe-container {
    background-color: #f5f5f5;
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
}

section {
    background: white;
    padding: 30px;
    border-radius: 15px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    width: 400px;
}

h1 {
    text-align: center;
    font-size: 24px;
    color: #333;
}

form {
    display: flex;
    flex-direction: column;
}

.input-group {
    position: relative;
    margin-bottom: 20px;
}

label {
    margin-bottom: 5px;
    font-weight: bold;
    text-align: left !important;
}

input {
    padding: 12px;
    border: 1px solid #ccc;
    border-radius: 8px;
    transition: border-color 0.3s;
    width: 100%;
}

input:focus {
    border-color: #42b983;
    outline: none;
}

.dropdown-list {
    list-style-type: none;
    padding: 0;
    margin: 0;
    border: 1px solid #ccc;
    border-radius: 8px;
    max-height: 150px;
    overflow-y: auto;
    position: absolute;
    background: white;
    z-index: 10;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
    width: 100%;
}

.dropdown-list li {
    padding: 10px;
    cursor: pointer;
    text-align: left !important;
    transition: background-color 0.3s;
}

.dropdown-list li:hover {
    background-color: #f0f0f0;
}

button {
    padding: 12px;
    background-color: #42b983;
    color: white;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: background-color 0.3s;
}

button:hover {
    background-color: #36a76a;
}
</style>
