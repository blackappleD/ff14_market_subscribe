<template>
    <div class="subscribe-container">
        <div class="nav-bar">
            <router-link to="/" class="nav-item">首页</router-link>
            <router-link to="/subscribe" class="nav-item active">物品订阅</router-link>
        </div>
        <div class="content">
            <h2 class="page-title">物品订阅</h2>
            <div class="subscription-form">
                <!-- 区服组列表 -->
                <div v-for="(group, groupIndex) in subscriptionGroups" :key="groupIndex" class="group">
                    <div class="input-row">
                        <div class="server-column">
                            <input v-model="group.worldSearch" class="input-field" placeholder="请选择区服"
                                @input="searchWorlds(groupIndex)" @focus="showWorldDropdown(groupIndex)"
                                @blur="hideDropdown('world', groupIndex)">
                            <!-- 区服下拉菜单 -->
                            <div v-if="group.showWorldDropdown && group.worldResults.length" class="dropdown-menu">
                                <div v-for="world in group.worldResults" :key="world.id" class="dropdown-item"
                                    @mousedown="selectWorld(groupIndex, world)">
                                    {{ world.name }}
                                </div>
                            </div>
                        </div>
                        <div class="items-column">
                            <div class="item-wrapper">
                                <input v-model="group.items[0].nameSearch" class="input-field" placeholder="物品名称"
                                    @input="searchItems(groupIndex, 0)" @focus="showItemDropdown(groupIndex, 0)"
                                    @blur="hideDropdown('item', groupIndex, 0)">

                                <div v-if="group.items[0].showDropdown && group.items[0].searchResults.length"
                                    class="dropdown-menu">
                                    <div v-for="item in group.items[0].searchResults" :key="item.id"
                                        class="dropdown-item" @mousedown="selectItem(groupIndex, 0, item)">
                                        {{ item.name }}
                                    </div>
                                </div>
                                <input v-model="group.items[0].threshold" class="input-field threshold" type="number"
                                    placeholder="价格阈值">
                                <button class="delete-button" @click="deleteItem(groupIndex, 0)">×</button>

                            </div>
                        </div>
                    </div>

                    <!-- 额外的物品行 -->
                    <div v-for="(item, itemIndex) in group.items.slice(1)" :key="itemIndex" class="input-row">
                        <div class="empty-field"></div>
                        <div class="item-input-wrapper">
                            <input v-model="item.nameSearch" class="input-field" placeholder="物品名称"
                                @input="searchItems(groupIndex, itemIndex + 1)"
                                @focus="showItemDropdown(groupIndex, itemIndex + 1)"
                                @blur="hideDropdown('item', groupIndex, itemIndex + 1)">
                            <!-- 物品下拉菜单 -->
                            <div v-if="item.showDropdown && item.searchResults.length" class="dropdown-menu">
                                <div v-for="result in item.searchResults" :key="result.id" class="dropdown-item"
                                    @mousedown="selectItem(groupIndex, itemIndex + 1, result)">
                                    {{ result.name }}
                                </div>
                            </div>
                        </div>
                        <input v-model="item.threshold" class="input-field" type="number" placeholder="价格阈值">
                        <button class="delete-button" @click="deleteItem(groupIndex, itemIndex + 1)">×</button>
                    </div>
                    <button class="add-item-button" @click="addItem(groupIndex)">
                        <span class="plus-icon">+</span>
                    </button>
                </div>

                <!-- 添加区服组按钮 -->
                <div class="server-add-row">
                    <button class="add-world-button" @click="addServerGroup">
                        <span class="plus-icon">+</span>
                    </button>
                </div>

                <!-- 添加提交按钮 -->
                <div class="submit-row">
                    <button class="submit-button" @click="handleSubmit">保存订阅</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import axios from '@/utils/axios';

// 修改 debounce ��数的类型定义
function debounce<T extends (...args: any[]) => any>(
    fn: T,
    delay: number
): (...args: Parameters<T>) => void {
    let timeoutId: number;
    return (...args: Parameters<T>) => {
        if (timeoutId) {
            window.clearTimeout(timeoutId);
        }
        timeoutId = window.setTimeout(() => fn(...args), delay);
    };
}

// 定义接口类型
interface WorldDTO {
    id: number;
    name: string;
    level: string;
}

interface ItemDTO {
    id: number;
    name: string;
}

interface ItemSubResDTO {
    id: number;
    item: ItemDTO;
    notifyThreshold: number;
}

interface UserSubscribeResDTO {
    world: WorldDTO;
    items: ItemSubResDTO[];
}

interface Item {
    id?: number;
    name?: string;
    nameSearch: string;
    threshold: number;
    showDropdown: boolean;
    searchResults: any[];
}

interface ServerGroup {
    worldId?: number;
    world?: string;
    worldSearch: string;
    showWorldDropdown: boolean;
    worldResults: any[];
    items: Item[];
}

interface ItemSubReqDTO {
    id?: number;
    item: {
        id: number;
    };
    notifyThreshold?: number;
}

interface UserSubscribeReqDTO {
    id?: number;
    world: {
        id: number;
    };
    items: ItemSubReqDTO[];
}

export default defineComponent({
    name: 'Subscribe',
    setup() {
        const subscriptionGroups = ref<ServerGroup[]>([{
            worldSearch: '',
            showWorldDropdown: false,
            worldResults: [],
            items: [{
                nameSearch: '',
                threshold: 0,
                showDropdown: false,
                searchResults: []
            }]
        }]);

        // 修改区服搜索逻辑，分为初始加载和搜索两个函数
        const loadAllWorlds = async (groupIndex: number) => {
            try {
                const response = await axios.get('/ff14/world/search?name='); // 获取所有区服列表
                subscriptionGroups.value[groupIndex].worldResults = response.data;
            } catch (error) {
                console.error('获取区服列表失败:', error);
            }
        };

        const searchWorlds = debounce(async (groupIndex: number) => {
            const search = subscriptionGroups.value[groupIndex].worldSearch;
            if (!search) {
                await loadAllWorlds(groupIndex);
                return;
            }
            try {
                const response = await axios.get(`/ff14/world/search?name=${search}`);
                subscriptionGroups.value[groupIndex].worldResults = response.data;
            } catch (error) {
                console.error('搜索区服失败:', error);
            }
        }, 300);

        const searchItems = debounce(async (groupIndex: number, itemIndex: number) => {
            const search = subscriptionGroups.value[groupIndex].items[itemIndex].nameSearch;
            try {
                const response = await axios.get(`/ff14/item/search?name=${search}`);
                subscriptionGroups.value[groupIndex].items[itemIndex].searchResults = response.data;
            } catch (error) {
                console.error('搜索物品失败:', error);
            }
        }, 300);

        // 修改显示区服下拉列表的方法
        const showWorldDropdown = async (groupIndex: number) => {
            subscriptionGroups.value[groupIndex].showWorldDropdown = true;
            await loadAllWorlds(groupIndex); // 直接加载所有区服列表
        };

        // 修改显示物品下拉列表的方法
        const showItemDropdown = async (groupIndex: number, itemIndex: number) => {
            subscriptionGroups.value[groupIndex].items[itemIndex].showDropdown = true;
        };

        const hideDropdown = (type: 'world' | 'item', groupIndex: number, itemIndex?: number) => {
            setTimeout(() => {
                if (type === 'world') {
                    subscriptionGroups.value[groupIndex].showWorldDropdown = false;
                } else if (itemIndex !== undefined) {
                    subscriptionGroups.value[groupIndex].items[itemIndex].showDropdown = false;
                }
            }, 200);
        };

        const selectWorld = (groupIndex: number, world: any) => {
            const group = subscriptionGroups.value[groupIndex];
            group.worldId = world.id;
            group.world = world.name;
            group.worldSearch = world.name;
            group.showWorldDropdown = false;
        };

        const selectItem = (groupIndex: number, itemIndex: number, item: any) => {
            const itemData = subscriptionGroups.value[groupIndex].items[itemIndex];
            itemData.id = item.id;
            itemData.name = item.name;
            itemData.nameSearch = item.name;
            itemData.showDropdown = false;
        };

        const addServerGroup = () => {
            subscriptionGroups.value.push({
                worldSearch: '',
                showWorldDropdown: false,
                worldResults: [],
                items: [{
                    nameSearch: '',
                    threshold: 0,
                    showDropdown: false,
                    searchResults: []
                }]
            });
        };

        const addItem = (groupIndex: number) => {
            subscriptionGroups.value[groupIndex].items.push({
                nameSearch: '',
                threshold: 0,
                showDropdown: false,
                searchResults: []
            });
        };

        const deleteItem = (groupIndex: number, itemIndex: number) => {
            const group = subscriptionGroups.value[groupIndex];
            if (group.items.length === 1) {
                // 如果是组内最后一个物品，删除整个组
                subscriptionGroups.value.splice(groupIndex, 1);
            } else {
                // 否则只删除该物品
                group.items.splice(itemIndex, 1);
            }
        };

        // 修改 fetchSubscriptions 函数
        const fetchSubscriptions = async () => {
            try {
                const response = await axios.get('/ff14/subscribe');
                const subscriptions: UserSubscribeResDTO[] = response.data;

                // 转换后端数据格式为前端格式
                subscriptionGroups.value = subscriptions.map(sub => ({
                    worldId: sub.world.id,
                    world: sub.world.name,
                    worldSearch: sub.world.name,
                    showWorldDropdown: false,
                    worldResults: [],
                    items: sub.items.map(itemSub => ({
                        id: itemSub.item.id,
                        name: itemSub.item.name,
                        nameSearch: itemSub.item.name,
                        threshold: itemSub.notifyThreshold,
                        showDropdown: false,
                        searchResults: []
                    }))
                }));

                // 如果没有订阅数据，添加一个空的组
                if (subscriptionGroups.value.length === 0) {
                    subscriptionGroups.value.push({
                        worldSearch: '',
                        showWorldDropdown: false,
                        worldResults: [],
                        items: [{
                            nameSearch: '',
                            threshold: 0,
                            showDropdown: false,
                            searchResults: []
                        }]
                    });
                }
            } catch (error) {
                console.error('获取订阅信息失败:', error);
                // 如果获取失败，至少显示一个空的表单
                subscriptionGroups.value = [{
                    worldSearch: '',
                    showWorldDropdown: false,
                    worldResults: [],
                    items: [{
                        nameSearch: '',
                        threshold: 0,
                        showDropdown: false,
                        searchResults: []
                    }]
                }];
            }
        };

        // 添加提交处理函数
        const handleSubmit = async () => {
            try {
                // 转换数据格式
                const subscribeData: UserSubscribeReqDTO[] = subscriptionGroups.value
                    .filter(group => group.worldId && group.items.length > 0)
                    .map(group => ({
                        world: {
                            id: group.worldId!
                        },
                        items: group.items
                            .filter(item => item.id)
                            .map(item => {
                                const itemData: ItemSubReqDTO = {
                                    item: {
                                        id: item.id!
                                    }
                                };
                                // 只有当 threshold 有值且不为 0 时才添加
                                if (item.threshold) {
                                    itemData.notifyThreshold = item.threshold;
                                }
                                return itemData;
                            })
                    }));

                if (subscribeData.length === 0) {
                    alert('请至少添加一个有效的订阅项');
                    return;
                }

                // 检查每个组是否都有至少一个物品
                const invalidGroup = subscribeData.find(group => group.items.length === 0);
                if (invalidGroup) {
                    alert('每个区服至少需要订阅一个物品');
                    return;
                }

                // 调用保存接口
                await axios.post('/ff14/subscribe', subscribeData);
                alert('保存成功');

                // 重新获取最新数据
                await fetchSubscriptions();
            } catch (error: any) {
                console.error('保存订阅失败:', error);
                alert(error.response?.data?.message || '保存失败，请重试');
            }
        };

        // 在组件挂载时调用获取订阅信息的函数
        onMounted(() => {
            fetchSubscriptions();
        });

        return {
            subscriptionGroups,
            searchWorlds,
            searchItems,
            showWorldDropdown,
            showItemDropdown,
            hideDropdown,
            selectWorld,
            selectItem,
            addServerGroup,
            addItem,
            deleteItem,
            fetchSubscriptions,
            handleSubmit
        };
    }
});
</script>

<style scoped>
.subscribe-container {
    min-height: 100vh;
    background-color: #f5f5f5;
}

.nav-bar {
    padding: 15px 0;
    display: flex;
    gap: 30px;
    border-bottom: 1px solid #eee;
    margin-left: 20px;
    background-color: #fff;
}

.nav-item {
    text-decoration: none;
    color: #666;
    font-size: 14px;
    position: relative;
}

.nav-item.active {
    color: #4285f4;
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
    padding: 40px;
}

.page-title {
    font-size: 24px;
    color: #333;
    margin-bottom: 30px;
    font-weight: normal;
    text-align: left;
}

.subscription-form {
    position: relative;
    padding: 20px 0;
    background-color: #fff;
    border-radius: 4px;
}

.group {
    margin-bottom: 30px;
}

.input-row {
    display: flex;
    gap: 17px;
    margin-bottom: 10px;
    padding: 0 40px;
    align-items: flex-start;
}

.server-column {
    position: relative;
    width: 200px;
}

.items-column {
    flex: 1;
}

.item-wrapper {
    display: flex;
    gap: 15px;
    position: relative;
}

/* 调整输入框宽度 */
.input-field {
    height: 32px;
    padding: 0 12px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 14px;
    background-color: #fff;
}

.input-wrapper .input-field {
    width: 200px;
}

.threshold {
    width: 200px;
}

.dropdown-menu {
    position: absolute;
    width: 200px;
    background: white;
    border: 1px solid #ddd;
    border-radius: 4px;
    max-height: 200px;
    overflow-y: auto;
    z-index: 1000;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.dropdown-item {
    padding: 8px 12px;
    cursor: pointer;
}

.dropdown-item:hover {
    background-color: #f5f5f5;
}


.plus-icon {
    width: 20px;
    height: 20px;
    background-color: #4285f4;
    color: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    line-height: 1;
}

.add-server {
    margin: 20px 0 0 40px;
}

.empty-field {
    width: 200px;
}

input:focus {
    outline: none;
    border-color: #4285f4;
}

/* 移除number输入框的箭头 */
input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button {
    -webkit-appearance: none;
    margin: 0;
}

.add-world-button {
    background: none;
    border: none;
    padding: 0 40px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
}

.add-item-button {
    background: none;
    border: none;
    padding: 0 260px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
}

.delete-button {
    background: none;
    border: none;
    color: #999;
    cursor: pointer;
    font-size: 18px;
    padding: 0 8px;
    display: flex;
    align-items: center;
}

.delete-button:hover {
    color: #ff4444;
}

.submit-row {
    display: flex;
    justify-content: center;
    margin-top: 30px;
    padding: 0 40px;
}

.submit-button {
    background-color: #4285f4;
    color: white;
    border: none;
    border-radius: 4px;
    padding: 10px 30px;
    font-size: 14px;
    cursor: pointer;
    transition: background-color 0.3s;
}

.submit-button:hover {
    background-color: #3367d6;
}
</style>