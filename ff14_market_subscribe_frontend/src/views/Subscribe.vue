<template>
    <div class="subscribe-container">
        <Toast :visible="toast.show" :message="toast.message" :type="toast.type" />
        <div class="nav-bar">
            <router-link to="/" class="nav-item">首页</router-link>
            <router-link to="/subscribe" class="nav-item active">物品订阅</router-link>
            <router-link to="/realtime" class="nav-item">实时物价</router-link>
            <router-link to="/itemsearch" class="nav-item">物价查询</router-link>
        </div>
        <div class="content">
            <div class="title-section">
                <h2 class="page-title">物品订阅</h2>
                <div class="notify-toggle">
                    <label class="switch">
                        <input type="checkbox" v-model="notifyEnabled" @change="handleNotifyToggle">
                        <span class="slider round"></span>
                    </label>
                    <span class="notify-text">{{ notifyEnabled ? '已开启推送通知' : '已关闭推送通知' }}</span>
                </div>
                <p class="description"> *订阅物品会每隔半小时进行推送通知，目前支持的推送方式：邮件</p>
                <p class="description"> *满足价格阈值条件的物品，会在实时物价和物价推送中被标记为红色</p>
            </div>
            <div class="subscription-form">
                <!-- 服务器组列表 -->
                <div v-for="(group, groupIndex) in subscriptionGroups" :key="groupIndex" class="group">
                    <div class="input-row">
                        <div class="server-column">
                            <input v-model="group.worldSearch" class="input-field" placeholder="请选择服务器"
                                @input="searchWorlds(groupIndex)" @focus="showWorldDropdown(groupIndex)"
                                @blur="hideDropdown('world', groupIndex)">
                            <!-- 服务器下拉菜单 -->
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
                                <div class="threshold-wrapper">
                                    <input v-model="group.items[0].threshold" class="input-field" type="number"
                                        placeholder="价格阈值">
                                    <label class="hq-toggle">
                                        <input type="checkbox" v-model="group.items[0].hq">
                                        <span class="hq-label">HQ</span>
                                    </label>
                                </div>
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
                        <div class="threshold-wrapper">
                            <input v-model="item.threshold" class="input-field" type="number" placeholder="价格阈值">
                            <label class="hq-toggle">
                                <input type="checkbox" v-model="item.hq">
                                <span class="hq-label">HQ</span>
                            </label>
                        </div>
                        <button class="delete-button" @click="deleteItem(groupIndex, itemIndex + 1)">×</button>
                    </div>
                    <button class="add-item-button" @click="addItem(groupIndex)">
                        <el-icon class="el-icon">
                            <Plus />
                        </el-icon>
                        <span class="button-text">添加物品</span>
                    </button>
                </div>

                <!-- 添加分组组按钮 -->
                <div class="server-add-row">
                    <button class="add-world-button" @click="addServerGroup">
                        <el-icon class="el-icon">
                            <Plus />
                        </el-icon>
                        <span class="button-text">添加分组</span>
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
import { defineComponent, ref, onMounted, computed } from 'vue';
import axios from '@/utils/axios';
import Toast from '@/components/Toast.vue';
import { Plus } from '@element-plus/icons-vue';
import { useStore } from 'vuex';
import { World } from '@/store/modules/world';

// 修改 debounce 函数的类型定义
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
interface ItemDTO {
    id: number;
    name: string;
}

interface ItemSubResDTO {
    id: number;
    item: ItemDTO;
    hq: boolean;
    notifyThreshold: number;
}

interface UserSubscribeResDTO {
    id: number;
    world: World;
    items: ItemSubResDTO[];
}

interface Item {
    subId?: number;
    id?: number;
    name?: string;
    nameSearch: string;
    threshold: number;
    hq: boolean;
    showDropdown: boolean;
    searchResults: any[];
}

interface ServerGroup {
    id?: number;
    worldId?: string;
    world?: string;
    worldSearch: string;
    showWorldDropdown: boolean;
    worldResults: World[];
    items: Item[];
}

interface ItemSubReqDTO {
    id?: number;
    item: {
        id: number;
    };
    notifyThreshold?: number;
    hq: boolean;
}

interface UserSubscribeReqDTO {
    id?: number;
    world: {
        id: string;
    };
    items: ItemSubReqDTO[];
}

export default defineComponent({
    name: 'Subscribe',
    components: {
        Toast,
        Plus
    },
    setup() {
        const store = useStore();
        const subscriptionGroups = ref<ServerGroup[]>([{
            worldSearch: '',
            showWorldDropdown: false,
            worldResults: [],
            items: [{
                nameSearch: '',
                threshold: 0,
                hq: false,
                showDropdown: false,
                searchResults: []
            }]
        }]);

        const toast = ref({
            show: false,
            message: '',
            type: 'success'
        });

        const showToast = (message: string, type = 'success') => {
            toast.value.message = message;
            toast.value.type = type;
            toast.value.show = true;
            setTimeout(() => {
                toast.value.show = false;
            }, 3000);
        };

        const notifyEnabled = ref(true);

        // Get all worlds from Vuex store
        const allWorlds = computed(() => store.getters['world/getWorlds']);

        // 获取通知配置
        const fetchNotifyConfig = async () => {
            try {
                const response = await axios.get('/ff14/sub_cfg');
                notifyEnabled.value = response.data.data.notify;
            } catch (error) {
                console.error('获取通知配置失败:', error);
            }
        };

        // 处理通知开关切换
        const handleNotifyToggle = async () => {
            try {
                await axios.put(`/ff14/sub_cfg/notify?notify=${notifyEnabled.value}`);
                showToast(notifyEnabled.value ? '已开启推送通知' : '已关闭推送通知');
            } catch (error: any) {
                console.error('修改通知配置失败:', error);
                notifyEnabled.value = !notifyEnabled.value; // 切换失败时恢复状态
                showToast(error.response?.data?.message || '修改通知配置失败', 'error');
            }
        };

        // MODIFIED: This function now filters the local `worlds` ref
        const searchWorlds = (groupIndex: number) => {
            const group = subscriptionGroups.value[groupIndex];
            if (!group.worldSearch) {
                group.worldResults = allWorlds.value;
            } else {
                group.worldResults = allWorlds.value.filter((world: World) =>
                    world.name.toLowerCase().includes(group.worldSearch.toLowerCase())
                );
            }
        };

        const debouncedSearchItems = debounce(async (groupIndex: number, itemIndex: number) => {
            const item = subscriptionGroups.value[groupIndex].items[itemIndex];
            const search = item.nameSearch;
            try {
                const response = await axios.get(`/ff14/item/search?name=${search}`);
                item.searchResults = response.data.data;
            } catch (error) {
                console.error('搜索物品失败:', error);
                item.searchResults = [];
            }
        }, 300);

        const searchItems = (groupIndex: number, itemIndex: number) => {
            debouncedSearchItems(groupIndex, itemIndex);
        };

        const showWorldDropdown = (groupIndex: number) => {
            const group = subscriptionGroups.value[groupIndex];
            searchWorlds(groupIndex); // Populate with filtered results on focus
            group.showWorldDropdown = true;
        };

        const showItemDropdown = (groupIndex: number, itemIndex: number) => {
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

        const selectWorld = (groupIndex: number, world: World) => {
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
                    hq: false,
                    showDropdown: false,
                    searchResults: []
                }]
            });
        };

        const addItem = (groupIndex: number) => {
            subscriptionGroups.value[groupIndex].items.push({
                nameSearch: '',
                threshold: 0,
                hq: false,
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
                const data = response.data.data;

                // 转换后端数据格式为前端格式，保留 ID
                subscriptionGroups.value = data.map((sub: UserSubscribeResDTO) => ({
                    id: sub.id,
                    worldId: sub.world.id,
                    world: sub.world.name,
                    worldSearch: sub.world.name,
                    showWorldDropdown: false,
                    worldResults: [],
                    items: sub.items.map(itemSub => ({
                        id: itemSub.item.id,
                        subId: itemSub.id,
                        name: itemSub.item.name,
                        nameSearch: itemSub.item.name,
                        threshold: itemSub.notifyThreshold,
                        hq: itemSub.hq,
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
                            hq: false,
                            showDropdown: false,
                            searchResults: []
                        }]
                    });
                }

                showToast('订阅加载成功');
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
                        hq: false,
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
                        id: group.id,
                        world: {
                            id: group.worldId!
                        },
                        items: group.items
                            .filter(item => item.id)
                            .map(item => {
                                const itemData: ItemSubReqDTO = {
                                    id: item.subId,
                                    item: {
                                        id: item.id!
                                    },
                                    hq: item.hq,
                                    notifyThreshold: item.threshold || undefined
                                };
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
                    alert('每个服务器至少需要订阅一个物品');
                    return;
                }

                // 调用保存接口并获取返回数据
                const response = await axios.post('/ff14/subscribe', subscribeData);
                showToast(response.data.message || '保存成功');

                // 使用返回的数据更新本地状态
                await fetchSubscriptions();
            } catch (error: any) {
                console.error('保存订阅失败:', error);
                showToast(error.response?.data?.message || error.response?.data?.data || '保存失败，请重试', 'error');
            }
        };

        // 在组件挂载时调用获取订阅信息的函数
        onMounted(() => {
            store.dispatch('world/fetchWorlds'); // Fetch worlds via Vuex
            fetchSubscriptions();
            fetchNotifyConfig();
        });

        return {
            subscriptionGroups,
            notifyEnabled,
            toast,
            handleNotifyToggle,
            addServerGroup,
            addItem,
            deleteItem,
            searchWorlds,
            searchItems,
            selectWorld,
            selectItem,
            handleSubmit,
            showToast,
            showWorldDropdown,
            showItemDropdown,
            hideDropdown
        };
    }
});
</script>

<style scoped>
.subscribe-container {
    min-height: 100vh;
    background-color: #ffffff;
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
    padding: 20px;
    max-width: 1200px;
    margin: 0 auto;
}

.title-section {
    margin-bottom: 20px;
}

.page-title {
    font-size: 24px;
    color: #333;
    margin: 0;
}

.notify-toggle {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
}

.notify-text {
    margin-left: 10px;
    color: #666;
    font-size: 14px;
}

.description {
    font-size: 11px;
    color: #666;
    margin: 0;
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
    gap: 15px;
    margin-bottom: 10px;
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

.add-world-button {
    background: none;
    border: 1px solid #4285f4;
    width: 196px;
    height: 30px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: left;
    gap: 8px;
    color: #4285f4;
    padding: 8px 16px;
    border-radius: 8px;
    transition: background-color 0.3s;
}

.add-item-button {
    background: none;
    border: 1px solid #4285f4;
    width: 196px;
    height: 30px;
    margin-left: 217px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: 10px;
    gap: 8px;
    color: #4285f4;
    padding: 8px 16px;
    border-radius: 8px;
    transition: background-color 0.3s;
}

.add-world-button:hover,
.add-item-button:hover {
    background-color: #f0f7ff;
    border-color: #3367d6;
    color: #3367d6;
}

.el-icon {
    font-size: 16px;

}

.button-text {
    font-size: 14px;
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

.threshold-wrapper {
    position: relative;
    display: flex;
    align-items: center;
}

.threshold-wrapper input {
    width: 150px;
    padding-right: 40px;
    /* 为HQ标签留出空间 */
}

.hq-toggle {
    position: absolute;
    right: 5px;
    display: flex;
    align-items: center;
    cursor: pointer;
    user-select: none;
}

.hq-toggle input[type="checkbox"] {
    position: absolute;
    opacity: 0;
    cursor: pointer;
    height: 0;
    width: 0;
}

.hq-label {
    font-size: 12px;
    color: #999;
    padding: 2px 4px;
    border-radius: 2px;
    transition: all 0.3s;
}

.hq-toggle input:checked+.hq-label {
    background-color: #4285f4;
    color: white;
}

/* 开关样式 */
.switch {
    position: relative;
    display: inline-block;
    width: 50px;
    height: 24px;
}

.switch input {
    opacity: 0;
    width: 0;
    height: 0;
}

.slider {
    position: absolute;
    cursor: pointer;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: #ccc;
    transition: .4s;
}

.slider:before {
    position: absolute;
    content: "";
    height: 16px;
    width: 16px;
    left: 4px;
    bottom: 4px;
    background-color: white;
    transition: .4s;
}

input:checked + .slider {
    background-color: #4285f4;
}

input:checked + .slider:before {
    transform: translateX(26px);
}

.slider.round {
    border-radius: 24px;
}

.slider.round:before {
    border-radius: 50%;
}
</style>