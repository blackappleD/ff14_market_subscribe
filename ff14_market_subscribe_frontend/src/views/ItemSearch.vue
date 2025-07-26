<template>
    <div class="item-search-container">
        <div class="nav-bar">
            <router-link to="/" class="nav-item">首页</router-link>
            <router-link to="/subscribe" class="nav-item">物品订阅</router-link>
            <router-link to="/realtime" class="nav-item">实时物价</router-link>
            <router-link to="/itemsearch" class="nav-item active">物价查询</router-link>
        </div>
        <div class="content">
            <div class="header">
                <h2 class="page-title">物价查询</h2>
            </div>
            
            <div class="search-section">
                <!-- 先选择服务器 -->
                <div class="world-selector" :class="{'highlight-error': worldError}">
                    <label>服务器：</label>
                    <input 
                        v-model="worldSearchText" 
                        @input="searchWorlds" 
                        @focus="showWorldDropdown = true" 
                        @blur="hideWorldDropdown"
                        placeholder="输入以查询服务器"
                        class="world-input"
                        :class="{'error-border': worldError}"
                    />
                    <div v-if="showWorldDropdown && worldResults.length > 0" class="dropdown-menu">
                        <div 
                            v-for="world in worldResults" 
                            :key="world.id" 
                            class="dropdown-item"
                            @mousedown="selectWorld(world)"
                        >
                            {{ world.name }}
                        </div>
                    </div>
                </div>
                
                <!-- 再搜索物品 -->
                <div class="search-container">
                    <div class="item-input-wrapper">
                        <input 
                            v-model="searchQuery" 
                            @input="debouncedSearchItems" 
                            @focus="handleItemInputFocus"
                            @blur="hideDropdown"
                            placeholder="输入物品名称搜索..." 
                            class="search-input"
                        />
                        <label class="hq-toggle">
                            <input type="checkbox" v-model="hqOnly">
                            <span class="hq-label">HQ</span>
                        </label>
                    </div>
                    <div v-if="showDropdown && searchResults.length > 0" class="dropdown-menu">
                        <div 
                            v-for="item in searchResults" 
                            :key="item.id" 
                            class="dropdown-item"
                            @mousedown="selectItem(item)"
                        >
                            {{ item.name }}
                        </div>
                    </div>
                    <div v-if="showHistory && searchHistory.length > 0" class="dropdown-menu history-dropdown">
                        <div class="dropdown-item history-item" @mousedown="showSearchHistory">
                            搜索历史
                        </div>
                        <div 
                            v-for="item in searchHistory" 
                            :key="item.id" 
                            class="dropdown-item history-item"
                            @mousedown="selectItem(item)"
                        >
                            {{ item.name }}
                        </div>
                    </div>
                </div>
                
                <button 
                    @click="searchPrice" 
                    class="search-button" 
                    :disabled="loading || !selectedItem"
                >
                    <div v-if="loading" class="loader"></div>
                    <span v-else>查询物价</span>
                </button>
            </div>
            
            <!-- 查询结果区域 -->
            <div v-if="priceData.length > 0" class="price-results">
                <table class="price-table">
                    <thead>
                        <tr>
                            <th style="width: 15%;">物品名称</th>
                            <th style="width: 10%;">所在服务器</th>
                            <th style="width: 15%;">单价</th>
                            <th style="width: 5%;">数量</th>
                            <th style="width: 10%;">手续费</th>
                            <th style="width: 10%;">总价</th>
                            <th style="width: 5%;">品质</th>
                            <th style="width: 15%;">雇员名称</th>
                            <th style="width: 15%;">生产者</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(price, index) in priceData" :key="index">
                            <td>{{ selectedItem.name }}</td>
                            <td>{{ price.worldName }}</td>
                            <td>{{ formatPrice(price.pricePerUnit) }}</td>
                            <td>{{ price.quantity }}</td>
                            <td>{{ formatPrice(price.tax) }}</td>
                            <td>{{ formatPrice(price.total) }}</td>
                            <td>{{ price.hq ? 'HQ' : 'NQ' }}</td>
                            <td>{{ price.retainerName || '-' }}</td>
                            <td>{{ price.creatorName || '-' }}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
            
            <!-- 加载动画 -->
            <div v-else-if="loading" class="loading-container">
                <div class="loader-large"></div>
                <div class="loading-text">正在加载物价数据...</div>
            </div>
            
            <!-- 无结果提示 -->
            <div v-else-if="hasSearched" class="no-data">
                暂无物价数据
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, computed } from 'vue';
import axios from '@/utils/axios';
import { useStore } from 'vuex';
import { ElMessage } from 'element-plus';
import { World } from '@/store/modules/world';

// Constants for localStorage keys
const SELECTED_WORLD_KEY = 'ff14_selected_world';
const SEARCH_HISTORY_KEY = 'ff14_item_search_history';
const MAX_HISTORY_ITEMS = 10;

// 防抖函数
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

interface ItemDTO {
    id: number;
    name: string;
}

interface ItemPriceInfo {
    pricePerUnit: number;
    quantity: number;
    tax: number;
    total: number;
    hq: boolean;
    creatorName: string;
    retainerName: string;
    worldName: string;
}

export default defineComponent({
    name: 'ItemSearch',
    setup() {
        const store = useStore();
        const searchQuery = ref('');
        const searchResults = ref<ItemDTO[]>([]);
        const selectedItem = ref<ItemDTO | null>(null);
        const selectedWorld = ref<World | null>(null);
        const worldSearchText = ref('');
        const worldResults = ref<World[]>([]);
        const priceData = ref<ItemPriceInfo[]>([]);
        const loading = ref(false);
        const hasSearched = ref(false);
        const showDropdown = ref(false);
        const showWorldDropdown = ref(false);
        const hqOnly = ref(false);
        const worldError = ref(false);
        const searchHistory = ref<ItemDTO[]>([]);
        const showHistory = ref(false);
        
        // Get all worlds from Vuex store
        const allWorlds = computed(() => store.getters['world/getWorlds']);
        
        // 加载历史记录
        const loadSearchHistory = () => {
            const historyString = localStorage.getItem(SEARCH_HISTORY_KEY);
            if (historyString) {
                try {
                    searchHistory.value = JSON.parse(historyString);
                } catch (e) {
                    console.error('Failed to parse search history:', e);
                    searchHistory.value = [];
                }
            }
        };
        
        // 保存历史记录
        const saveSearchHistory = () => {
            localStorage.setItem(SEARCH_HISTORY_KEY, JSON.stringify(searchHistory.value));
        };
        
        // 添加到历史记录
        const addToHistory = (item: ItemDTO) => {
            // 检查是否已存在，如果存在则删除旧的
            const index = searchHistory.value.findIndex(i => i.id === item.id);
            if (index !== -1) {
                searchHistory.value.splice(index, 1);
            }
            
            // 添加到数组开头（最新的在前面）
            searchHistory.value.unshift(item);
            
            // 限制最多10条
            if (searchHistory.value.length > MAX_HISTORY_ITEMS) {
                searchHistory.value = searchHistory.value.slice(0, MAX_HISTORY_ITEMS);
            }
            
            // 保存到localStorage
            saveSearchHistory();
        };
        
        // 显示历史记录
        const showSearchHistory = () => {
            if (searchHistory.value.length > 0) {
                showHistory.value = true;
                showDropdown.value = true;
            }
        };
        
        // 搜索物品
        const searchItems = async () => {
            if (!searchQuery.value) {
                searchResults.value = [];
                return;
            }
            
            try {
                const response = await axios.get(`/ff14/item/search?name=${encodeURIComponent(searchQuery.value)}`);
                searchResults.value = response.data.data || [];
                
                // 如果有搜索结果，显示下拉框
                if (searchResults.value.length > 0) {
                    showDropdown.value = true;
                    showHistory.value = false;
                }
            } catch (error: any) {
                console.error('搜索物品失败:', error);
                ElMessage.error(error.response?.data?.message || '搜索物品失败');
                searchResults.value = [];
            }
        };
        
        // 防抖搜索
        const debouncedSearchItems = debounce(() => {
            searchItems();
            showHistory.value = false; // 开始搜索时隐藏历史记录
        }, 500);
        
        // 选择物品
        const selectItem = (item: ItemDTO) => {
            selectedItem.value = item;
            showDropdown.value = false;
            showHistory.value = false;
            searchQuery.value = item.name;
            
            // 添加到历史记录
            addToHistory(item);
        };
        
        // 保存选中的服务器到localStorage
        const saveSelectedWorld = (world: World) => {
            localStorage.setItem(SELECTED_WORLD_KEY, JSON.stringify({
                id: world.id,
                name: world.name
            }));
        };
        
        // 从localStorage加载上次选择的服务器
        const loadSelectedWorld = () => {
            const savedWorld = localStorage.getItem(SELECTED_WORLD_KEY);
            if (savedWorld && allWorlds.value.length > 0) {
                try {
                    const parsed = JSON.parse(savedWorld);
                    const foundWorld = allWorlds.value.find((w: World) => w.id === parsed.id);
                    if (foundWorld) {
                        selectedWorld.value = foundWorld;
                        worldSearchText.value = foundWorld.name;
                    }
                } catch (e) {
                    console.error('Failed to parse saved world:', e);
                }
            }
        };
        
        // 服务器搜索
        const searchWorlds = () => {
            if (!worldSearchText.value) {
                worldResults.value = allWorlds.value;
                worldError.value = false;
            } else {
                worldResults.value = allWorlds.value.filter((world: World) => 
                    world.name.toLowerCase().includes(worldSearchText.value.toLowerCase())
                );
                if (worldResults.value.length === 0) {
                    worldError.value = true;
                } else {
                    worldError.value = false;
                }
            }
        };
        
        // 选择服务器
        const selectWorld = (world: World) => {
            selectedWorld.value = world;
            showWorldDropdown.value = false;
            worldSearchText.value = world.name;
            worldError.value = false;
            
            // 保存选择的服务器
            saveSelectedWorld(world);
        };
        
        // 隐藏下拉框
        const hideDropdown = () => {
            setTimeout(() => {
                showDropdown.value = false;
                showHistory.value = false;
            }, 200);
        };
        
        const hideWorldDropdown = () => {
            setTimeout(() => {
                showWorldDropdown.value = false;
            }, 200);
        };
        
        // 显示搜索历史或开始搜索
        const handleItemInputFocus = () => {
            if (searchQuery.value) {
                showDropdown.value = true;
            } else {
                showSearchHistory();
            }
        };
        
        // 搜索物价方法
        const searchPrice = async () => {
            if (!selectedItem.value) {
                ElMessage.warning('请选择物品');
                return;
            }
            
            if (!selectedWorld.value) {
                worldError.value = true;
                ElMessage.warning('请先选择服务器');
                return;
            }
            
            loading.value = true;
            hasSearched.value = true;
            
            try {
                const response = await axios.get(`/ff14/price/on_time/${selectedWorld.value.name}/${selectedItem.value.id}?hq=${hqOnly.value}`);
                priceData.value = response.data.data || [];
                
                if (priceData.value.length === 0) {
                    ElMessage.info('没有找到相关物价信息');
                }
            } catch (error: any) {
                console.error('查询物价失败:', error);
                ElMessage.error(error.response?.data?.message || '查询物价失败');
                priceData.value = [];
            } finally {
                loading.value = false;
            }
        };
        
        // 格式化价格
        const formatPrice = (price: number | null | undefined): string => {
            return price?.toLocaleString() || '0';
        };
        
        // 初始化
        onMounted(async () => {
            await store.dispatch('world/fetchWorlds');
            worldResults.value = allWorlds.value;
            
            // 加载历史记录和上次选择的服务器
            loadSearchHistory();
            loadSelectedWorld();
        });
        
        return {
            searchQuery,
            searchResults,
            selectedItem,
            selectedWorld,
            worldSearchText,
            worldResults,
            priceData,
            loading,
            hasSearched,
            showDropdown,
            showWorldDropdown,
            hqOnly,
            debouncedSearchItems,
            selectItem,
            searchWorlds,
            selectWorld,
            hideDropdown,
            hideWorldDropdown,
            searchPrice,
            formatPrice,
            worldError,
            searchHistory,
            showHistory,
            handleItemInputFocus
        };
    }
});
</script>

<style scoped>
.item-search-container {
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

.nav-item.active {
    color: #4285f4;
}

.content {
    padding: 20px;
    max-width: 1200px;
    margin: 0 auto;
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.page-title {
    font-size: 24px;
    color: #333;
    margin: 0;
}

.search-section {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 30px;
    flex-wrap: wrap;
}

.search-container {
    position: relative;
    flex: 1;
    min-width: 250px;
}

.item-input-wrapper {
    position: relative;
    width: 100%;
}

.search-container.disabled {
    opacity: 0.6;
}

.search-input {
    width: 100%;
    padding: 10px 15px;
    padding-right: 50px; /* Make space for the HQ toggle */
    border: 1px solid #ccc;
    border-radius: 4px;
    font-size: 16px;
    transition: border-color 0.3s;
}

.search-input:focus {
    outline: none;
    border-color: #4285f4;
}

.search-input:disabled {
    background-color: #f5f5f5;
    cursor: not-allowed;
}

.hq-toggle {
    position: absolute;
    right: 5px;
    top: 50%;
    transform: translateY(-50%);
    display: flex;
    align-items: center;
    cursor: pointer;
    z-index: 1;
}

.hq-toggle input {
    display: none;
}

.hq-label {
    background-color: #f0f0f0;
    padding: 5px 10px;
    border: 1px solid #ccc;
    border-radius: 4px;
    font-size: 14px;
    color: #666;
    transition: all 0.3s;
}

.hq-toggle input:checked + .hq-label {
    background-color: #4285f4;
    color: white;
    border-color: #4285f4;
}

.world-selector {
    display: flex;
    align-items: center;
    gap: 10px;
    position: relative;
    min-width: 230px;
}

.world-input {
    border: 1px solid #ccc;
    border-radius: 4px;
    padding: 10px 15px;
    font-size: 14px;
    width: 150px;
    transition: border-color 0.2s;
}

.world-input:focus {
    outline: none;
    border-color: #4285f4;
}

.world-input.highlight-error {
    border-color: #f56c6c; /* Red border for error */
}

.dropdown-menu {
    position: absolute;
    top: 100%;
    left: 0;
    background-color: white;
    border: 1px solid #ccc;
    border-radius: 4px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    z-index: 100;
    max-height: 200px;
    overflow-y: auto;
    width: 100%;
    margin-top: 4px;
}

.dropdown-item {
    padding: 10px 15px;
    cursor: pointer;
    transition: background-color 0.2s;
}

.dropdown-item:hover {
    background-color: #f0f0f0;
}

.search-button {
    height: 42px;
    padding: 0 20px;
    background-color: #4285f4;
    border: none;
    border-radius: 4px;
    color: white;
    font-size: 16px;
    cursor: pointer;
    transition: background-color 0.3s;
    min-width: 120px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.search-button:hover:not(:disabled) {
    background-color: #3367d6;
}

.search-button:disabled {
    background-color: #ccc;
    cursor: not-allowed;
}

.search-button .loader {
    width: 20px;
    height: 20px;
}

.price-results {
    margin-top: 30px;
}

.price-table {
    width: 100%;
    border-collapse: collapse;
    table-layout: fixed;
}

.price-table th,
.price-table td {
    padding: 12px;
    text-align: left;
    border-bottom: 1px solid #eee;
}

.price-table th {
    background: #fafafa;
    font-weight: 500;
    color: #666;
}

.no-data {
    text-align: center;
    padding: 40px;
    color: #666;
    background: #f9f9f9;
    border-radius: 8px;
    margin-top: 30px;
}

.hq-filter {
    margin-top: 15px;
    display: flex;
    justify-content: flex-end;
}

.hq-filter label {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    font-size: 14px;
    color: #666;
}

/* Loading animation */
.loader {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: #ffffff;
    -webkit-mask: radial-gradient(circle closest-side at 50% 40%, #0000 94%, #fff);
    transform-origin: 50% 40%;
    animation: loaderRotate 1s infinite linear;
}

@keyframes loaderRotate {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

.error-border {
    border-color: #f56c6c !important;
    animation: errorPulse 1s;
}

@keyframes errorPulse {
    0% { box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.7); }
    70% { box-shadow: 0 0 0 5px rgba(245, 108, 108, 0); }
    100% { box-shadow: 0 0 0 0 rgba(245, 108, 108, 0); }
}

/* Additional CSS for loading animation */
.loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 200px;
    background: #f9f9f9;
    border-radius: 8px;
    margin-top: 30px;
}

.loader-large {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    background: #000000;
    -webkit-mask: radial-gradient(circle closest-side at 50% 40%, #0000 94%, #000);
    transform-origin: 50% 40%;
    animation: loaderRotate 1s infinite linear;
    margin-bottom: 20px;
}

.loading-text {
    font-size: 16px;
    color: #666;
}

/* Styles for history dropdown */
.history-dropdown {
    position: absolute;
    top: 100%; /* Position below the search input */
    left: 0;
    background-color: white;
    border: 1px solid #ccc;
    border-radius: 4px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    z-index: 99; /* Lower z-index than dropdown-menu */
    max-height: 200px;
    overflow-y: auto;
    width: 100%;
    margin-top: 4px;
    padding: 5px 0; /* Add some padding for the history item */
}

.history-item {
    padding: 10px 15px;
    cursor: pointer;
    transition: background-color 0.2s;
    font-size: 14px;
    color: #666;
}

.history-item:hover {
    background-color: #f0f0f0;
}

.history-item:last-child {
    border-bottom: none;
}
</style> 