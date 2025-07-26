<template>
    <div class="realtime-price-container">
        <div class="nav-bar">
            <router-link to="/" class="nav-item">首页</router-link>
            <router-link to="/subscribe" class="nav-item">物品订阅</router-link>
            <router-link to="/realtime" class="nav-item active">实时物价</router-link>
            <router-link to="/itemsearch" class="nav-item">物价查询</router-link>
        </div>
        <div class="content">
            <div class="header">
                <h2 class="page-title">实时物价</h2>
                <div class="refresh-section">
                    <div class="settings-wrapper">
                        <div class="settings-item">
                            <label>显示条数：</label>
                            <select v-model="maxListings" @change="updateMaxListings" class="listings-select">
                                <option v-for="option in listingsOptions" :key="option" :value="option">{{ option }}</option>
                            </select>
                        </div>
                    </div>
                    <button @click="fetchPriceData" class="refresh-button" :disabled="loading || cooldownTime > 0">
                        <template v-if="loading">拉取中...</template>
                        <template v-else-if="cooldownTime > 0">
                            {{ cooldownTime }}秒后可刷新
                        </template>
                        <template v-else>拉取全部分组物价</template>
                    </button>
                    <span class="last-update" v-if="lastUpdateTime">
                        上次刷新: {{ formatTime(lastUpdateTime) }}
                    </span>
                </div>
            </div>
 
            <!-- 加载状态显示 -->
            <div v-if="isLoading" class="loading-state">
                分组信息加载中，请稍后...
            </div>

            <!-- 订阅实时价格分组展示 (MODIFIED) -->
            <template v-else>
                <div v-if="hasSubscriptions">
                    <div v-for="(subscribeGroup, groupIndex) in priceData" :key="subscribeGroup.id"
                        class="subscribe-group">
                        <div class="group-header">
                            <div class="world-selector">
                                <input v-model="subscribeGroup.worldSearchText" class="world-input"
                                    :placeholder="subscribeGroup.worldName" @input="searchWorlds(groupIndex)"
                                    @focus="showWorldDropdown(groupIndex)" @blur="hideDropdown(groupIndex)">
                                <div v-if="subscribeGroup.showWorldDropdown && subscribeGroup.worldResults.length"
                                    class="dropdown-menu">
                                    <div v-for="world in subscribeGroup.worldResults" :key="world.id"
                                        class="dropdown-item" @mousedown="selectWorld(groupIndex, world)">
                                        {{ world.name }}
                                    </div>
                                </div>
                            </div>
                            <button class="action-button" @click="refreshGroup(subscribeGroup.id)" :disabled="groupLoadingStates[subscribeGroup.id]">
                                <div v-if="groupLoadingStates[subscribeGroup.id]" class="loader"></div>
                                <span v-else>拉取分组物价</span>
                            </button>
                        </div>
                        <!-- 加载状态蒙版，只在加载时显示 -->
                        <div v-show="groupLoadingStates[subscribeGroup.id]" class="table-loading">
                            <div class="loader"></div>
                        </div>
                        <!-- 表格始终存在，加载时通过蒙版模糊效果显示 -->
                        <table class="price-table" :class="{'table-hidden': groupLoadingStates[subscribeGroup.id]}">
                            <thead>
                                <tr>
                                    <th style="width: 18%;">物品名称</th>
                                    <th style="width: 10%;">所在服务器</th>
                                    <th style="width: 15%;">单价</th>
                                    <th style="width: 5%;">数量</th>
                                    <th style="width: 10%;">手续费</th>
                                    <th style="width: 10%;">总价</th>
                                    <th style="width: 5%;">品质</th>
                                    <th style="width: 10%;">雇员名称</th>
                                    <th style="width: 10%;">生产者</th>
                                </tr>
                            </thead>
                            <tbody>
                                <template v-for="item in subscribeGroup.itemPriceGroups" :key="item.id">
                                    <tr class="main-row">
                                        <td>
                                            <div class="item-name-cell">
                                                <button class="collapse-button"
                                                    v-if="item.itemPriceInfoList && item.itemPriceInfoList.length > 0"
                                                    @click="toggleItemPrices(subscribeGroup.id, item.id)">
                                                    {{ isExpanded(subscribeGroup.id, item.id) ? '▼' : '▶' }}
                                                </button>
                                                <span v-else class="collapse-button-placeholder"></span>
                                                {{ item.name }}
                                            </div>
                                        </td>
                                        <template v-if="item.itemPriceInfoList && item.itemPriceInfoList.length > 0">
                                            <td>{{ item.itemPriceInfoList[0].worldName }}</td>
                                            <td :class="{ 'lower-price': item.itemPriceInfoList[0].lowerThreshold }">
                                                {{ formatPrice(item.itemPriceInfoList[0].pricePerUnit) }}
                                            </td>
                                            <td>{{ item.itemPriceInfoList[0].quantity }}</td>
                                            <td>{{ formatPrice(item.itemPriceInfoList[0].tax) }}</td>
                                            <td>{{ formatPrice(item.itemPriceInfoList[0].total) }}</td>
                                            <td>{{ item.itemPriceInfoList[0].hq ? 'HQ' : 'NQ' }}</td>
                                            <td>{{ item.itemPriceInfoList[0].retainerName || '-' }}</td>
                                            <td>{{ item.itemPriceInfoList[0].creatorName || '-' }}</td>
                                        </template>
                                        <template v-else>
                                            <td>
                                                <div class="item-name-cell">
                                                    <span class="collapse-button-placeholder"></span>
                                                    -
                                                </div>
                                            </td>
                                            <td colspan="7" class="no-price-info-cell">-</td>
                                        </template>
                                    </tr>
                                    <!-- 展开的额外价格信息 -->
                                    <template
                                        v-if="isExpanded(subscribeGroup.id, item.id) && item.itemPriceInfoList.length > 1">
                                        <tr v-for="(price, priceIndex) in item.itemPriceInfoList.slice(1)"
                                            :key="`${item.name}-${priceIndex}`" class="expanded-row">
                                            <td></td>
                                            <td>{{ price.worldName }}</td>
                                            <td :class="{ 'lower-price': price.lowerThreshold }">
                                                {{ formatPrice(price.pricePerUnit) }}
                                            </td>
                                            <td>{{ price.quantity }}</td>
                                            <td>{{ formatPrice(price.tax) }}</td>
                                            <td>{{ formatPrice(price.total) }}</td>
                                            <td>{{ price.hq ? 'HQ' : 'NQ' }}</td>
                                            <td>{{ price.retainerName || '-' }}</td>
                                            <td>{{ price.creatorName || '-' }}</td>
                                        </tr>
                                    </template>
                                </template>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- 无订阅数据提示 -->
                <div v-else class="no-data">
                    暂无订阅数据，请先
                    <router-link to="/subscribe" class="subscribe-link">前往订阅页面</router-link>
                    添加物品
                </div>
            </template>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, onUnmounted, computed } from 'vue';
import axios from '@/utils/axios';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useStore } from 'vuex';
import { World } from '@/store/modules/world';

// Debounce function from Subscribe.vue
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


interface PriceInfo {
    pricePerUnit: number;
    quantity: number;
    tax: number;
    total: number;
    hq: boolean;
    creatorName: string;
    retainerName: string;
    worldName: string;
    lowerThreshold: boolean;
}

interface ItemPriceGroup {
    id: number;
    name: string;
    itemPriceInfoList: PriceInfo[];
}

// MODIFIED
interface subscribePriceGroup {
    id: number;
    worldName: string;
    worldId: string; // Corrected: from number to string
    itemPriceGroups: ItemPriceGroup[];
    // New properties for search
    worldSearchText: string;
    showWorldDropdown: boolean;
    worldResults: World[];
}

interface UserConfig {
    id: number;
    notify: boolean;
    maxListings: number;
}

// Constants for localStorage keys
const COOLDOWN_KEY = 'price_refresh_cooldown';
const LAST_UPDATE_KEY = 'price_last_update';
const PRICE_DATA_KEY = 'price_data';

export default defineComponent({
    name: 'RealTimePrice',
    components: {
        // Removed ElSelect, ElOption
    },
    setup() {
        const store = useStore();
        const router = useRouter();
        const priceData = ref<subscribePriceGroup[]>([]);
        const loading = ref(false);
        const isLoading = ref(true);
        const hasSubscriptions = ref(false);
        const lastUpdateTime = ref<Date | null>(null);
        const cooldownTime = ref(0);
        let cooldownTimer: number | null = null;
        const expandedItems = ref(new Set<string>());
        const maxListings = ref(10);
        const listingsOptions = ref([5, 10, 20, 30, 50, 100]);
        const groupLoadingStates = ref<Record<number, boolean>>({});

        // 获取用户配置
        const fetchUserConfig = async () => {
            try {
                const response = await axios.get('/ff14/sub_cfg');
                const config: UserConfig = response.data.data;
                if (config && config.maxListings) {
                    maxListings.value = config.maxListings;
                }
            } catch (error) {
                console.error('获取用户配置失败:', error);
            }
        };

        // 更新maxListings设置
        const updateMaxListings = async () => {
            try {
                await axios.put(`/ff14/sub_cfg/max_listings?maxListings=${maxListings.value}`);
                ElMessage.success('显示条数配置已更新');
            } catch (error: any) {
                console.error('更新显示条数失败:', error);
                ElMessage.error(error.response?.data?.message || '更新显示条数失败');
            }
        };

        // Get all worlds from Vuex store
        const allWorlds = computed(() => store.getters['world/getWorlds']);

        // This function now filters the local `worlds` ref
        const searchWorlds = (groupIndex: number) => {
            const group = priceData.value[groupIndex];
            if (!group.worldSearchText) {
                group.worldResults = allWorlds.value; // Show all if input is empty
            } else {
                group.worldResults = allWorlds.value.filter((world: World) =>
                    world.name.toLowerCase().includes(group.worldSearchText.toLowerCase())
                );
            }
        };

        // Debounced auto-save function (no change to this)
        const debouncedSaveWorld = debounce(async (groupId: number, worldId: string) => {
            try {
                await axios.put(`/ff14/subscribe/${groupId}/world/${worldId}`);
                ElMessage.success('服务器已自动保存，正在刷新价格...');
                await refreshGroup(groupId);
            } catch (error: any) {
                console.error('自动保存服务器失败:', error);
                ElMessage.error(error.response?.data?.message || '自动保存服务器失败');
            }
        }, 1000);

        const selectWorld = (groupIndex: number, world: World) => {
            const group = priceData.value[groupIndex];
            group.worldId = world.id;
            group.worldSearchText = world.name; // Update input text before hiding
            group.showWorldDropdown = false;
            // Now, we save. The save function will trigger the refresh.
            debouncedSaveWorld(group.id, world.id);
        };

        // 保存价格数据到缓存
        const savePriceDataToCache = () => {
            try {
                localStorage.setItem(PRICE_DATA_KEY, JSON.stringify(priceData.value));
            } catch (e) {
                console.error('保存价格数据缓存失败:', e);
            }
        };

        // 从缓存加载价格数据
        const loadCachedPriceData = () => {
            try {
                const cachedData = localStorage.getItem(PRICE_DATA_KEY);
                if (cachedData) {
                    const parsedData = JSON.parse(cachedData);
                    
                    // 仅当有缓存数据且当前priceData结构已初始化时进行合并
                    if (parsedData && Array.isArray(parsedData) && priceData.value.length > 0) {
                        // 合并缓存数据到当前数据
                        parsedData.forEach((cachedGroup: any) => {
                            const currentGroup = priceData.value.find(g => g.id === cachedGroup.id);
                            if (currentGroup) {
                                // 将缓存的价格数据合并到当前组
                                cachedGroup.itemPriceGroups.forEach((cachedItem: any) => {
                                    const currentItem = currentGroup.itemPriceGroups.find(
                                        (item: any) => item.id === cachedItem.id
                                    );
                                    if (currentItem) {
                                        // 应用缓存的价格数据
                                        currentItem.itemPriceInfoList = cachedItem.itemPriceInfoList || [];
                                    }
                                });
                            }
                        });
                        
                        console.log('已加载缓存的价格数据');
                    }
                }
            } catch (e) {
                console.error('加载价格数据缓存失败:', e);
            }
        };

        // 更新 refreshGroup 函数以保存缓存
        const refreshGroup = async (groupId: number, showMessage = true) => {
            // Set loading state for this group
            groupLoadingStates.value[groupId] = true;
            
            try {
                const response = await axios.get(`/ff14/price/on_time/${groupId}`);
                const updatedGroupData = response.data.data; // Corrected: access response.data.data
                const index = priceData.value.findIndex(g => g.id === groupId);

                if (index !== -1 && updatedGroupData) {
                    // Preserve UI state from the old group
                    const oldGroup = priceData.value[index];
                    const newGroup = {
                        ...updatedGroupData,
                        worldId: updatedGroupData.world.id,
                        worldName: updatedGroupData.world.name,
                        itemPriceGroups: oldGroup.itemPriceGroups.map(oldItem => {
                            const newItem = updatedGroupData.itemPriceGroups?.find((i: any) => i.id === oldItem.id);
                            return newItem ? { ...oldItem, ...newItem } : oldItem;
                        }),
                        worldSearchText: oldGroup.worldSearchText,
                        showWorldDropdown: oldGroup.showWorldDropdown,
                        worldResults: oldGroup.worldResults,
                    };
                    priceData.value[index] = newGroup;

                    // 保存更新后的价格数据到缓存
                    savePriceDataToCache();
                    
                    if (showMessage) {
                        ElMessage.success(`分组 ${newGroup.worldName} 的价格已刷新`);
                    }
                } else if (!updatedGroupData) {
                     if (showMessage) ElMessage.error('刷新失败，未获取到分组数据');
                     throw new Error('未获取到分组数据');
                }
                return Promise.resolve();
            } catch (error: any) {
                console.error('刷新分组价格失败:', error);
                if (showMessage) ElMessage.error(error.response?.data?.message || '刷新分组价格失败');
                return Promise.reject(error);
            } finally {
                // Clear loading state regardless of success or failure
                groupLoadingStates.value[groupId] = false;
            }
        };

        const showWorldDropdown = (groupIndex: number) => {
            const group = priceData.value[groupIndex];
            // Populate dropdown with filtered results on focus
            searchWorlds(groupIndex); 
            group.showWorldDropdown = true;
        };

        const hideDropdown = (groupIndex: number) => {
            // Delay hiding to allow click event to register
            setTimeout(() => {
                if (priceData.value[groupIndex]) {
                    priceData.value[groupIndex].showWorldDropdown = false;
                }
            }, 200);
        };


        const fetchPriceData = async () => {
            if (loading.value || cooldownTime.value > 0) return;
            if (!priceData.value || priceData.value.length === 0) {
                ElMessage.info('没有可以刷新的分组');
                return;
            }

            loading.value = true;
            try {
                const refreshPromises = priceData.value.map(group => refreshGroup(group.id, false));
                await Promise.all(refreshPromises);

                lastUpdateTime.value = new Date();
                updateCooldownStorage();
                startCooldown();
                
                // 保存所有价格数据到缓存
                savePriceDataToCache();
                
                ElMessage.success('所有分组价格已刷新');
            } catch (error) {
                console.error('拉取全部分组物价失败:', error);
                ElMessage.error('部分分组刷新失败，请检查控制台');
            } finally {
                loading.value = false;
            }
        };

        const fetchSubscriptionGroups = async () => {
            isLoading.value = true;
            try {
                const response = await axios.get('/ff14/subscribe');
                const groups = response.data.data;

                if (groups && groups.length > 0) {
                    hasSubscriptions.value = true;
                    priceData.value = groups.map((group: any) => ({
                        id: group.id,
                        worldId: group.world.id,
                        worldName: group.world.name,
                        // Corrected mapping based on provided API response
                        itemPriceGroups: (group.items || []).map((subItem: any) => ({
                            id: subItem.item.id,
                            name: subItem.item.name,
                            itemPriceInfoList: [], // Price info is initially empty
                        })),
                        worldSearchText: group.world.name,
                        showWorldDropdown: false,
                        worldResults: [],
                    }));
                    
                    // 加载缓存的价格数据
                    loadCachedPriceData();
                } else {
                    hasSubscriptions.value = false;
                }
            } catch (error: any) {
                console.error('获取订阅分组失败:', error);
                if (error.response?.status === 401) {
                    router.push('/login');
                } else {
                    alert(error.response?.data?.message || '获取订阅分组失败');
                }
            } finally {
                isLoading.value = false;
            }
        };


        onMounted(async () => {
            await store.dispatch('world/fetchWorlds'); // Fetch worlds via Vuex
            await fetchUserConfig(); // 获取用户配置
            await fetchSubscriptionGroups();
            checkCooldown();
        });
        
        // ... other methods like startCooldown, updateCooldownStorage, formatPrice, etc. remain
        // ... but I'll paste them for completeness
        const startCooldown = () => {
            cooldownTime.value = cooldownTime.value || 120;
            if (cooldownTimer) {
                clearInterval(cooldownTimer);
            }
            cooldownTimer = window.setInterval(() => {
                if (cooldownTime.value > 0) {
                    cooldownTime.value--;
                    if (cooldownTime.value === 0) {
                        localStorage.removeItem(COOLDOWN_KEY);
                        localStorage.removeItem(LAST_UPDATE_KEY);
                    }
                } else if (cooldownTimer) {
                    clearInterval(cooldownTimer);
                    cooldownTimer = null;
                }
            }, 1000);
        };

        const checkCooldown = (): boolean => {
            const savedCooldown = localStorage.getItem(COOLDOWN_KEY);
            const savedLastUpdate = localStorage.getItem(LAST_UPDATE_KEY);

            if (savedCooldown && savedLastUpdate) {
                const now = Date.now();
                const lastUpdate = parseInt(savedLastUpdate);
                const remainingTime = Math.ceil((120000 - (now - lastUpdate)) / 1000);

                if (remainingTime > 0) {
                    cooldownTime.value = remainingTime;
                startCooldown();
                    return true;
                }
            }
            return false;
        };

        const updateCooldownStorage = () => {
            localStorage.setItem(COOLDOWN_KEY, 'true');
            localStorage.setItem(LAST_UPDATE_KEY, Date.now().toString());
        };

        onUnmounted(() => {
            if (cooldownTimer) {
                clearInterval(cooldownTimer);
            }
        });

        const formatPrice = (price: number | null | undefined): string => {
            return price?.toLocaleString() || '0';
        };

        const formatTime = (date: Date): string => {
            return new Date(date).toLocaleString();
        };

        const toggleItemPrices = (subGroupId: number, itemId: number) => {
            const key = `${subGroupId}-${itemId}`;
            if (expandedItems.value.has(key)) {
                expandedItems.value.delete(key);
            } else {
                expandedItems.value.add(key);
            }
        };

        const isExpanded = (subGroupId: number, itemId: number) => {
            return expandedItems.value.has(`${subGroupId}-${itemId}`);
        };


        return {
            priceData,
            loading,
            isLoading,
            hasSubscriptions,
            lastUpdateTime,
            cooldownTime,
            expandedItems,
            maxListings,
            listingsOptions,
            toggleItemPrices,
            isExpanded,
            fetchPriceData,
            formatTime,
            formatPrice,
            refreshGroup,
            updateMaxListings,
            // new methods
            searchWorlds,
            selectWorld,
            showWorldDropdown,
            hideDropdown,
            groupLoadingStates
        };
    }
});
</script>

<style scoped>
.realtime-price-container {
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

.subscribe-group {
    position: relative;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 20px;
    min-height: 200px; /* 确保有最小高度 */
}

.group-header {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    gap: 10px;
}

/* MODIFIED */
.world-selector {
    position: relative;
    display: flex;
    align-items: center;
}

.world-input {
    border: 1px solid #ccc;
    border-radius: 4px;
    padding: 6px 10px;
    font-size: 14px;
    width: 150px;
    transition: border-color 0.2s;
}

.world-input:focus {
    outline: none;
    border-color: #4285f4;
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
    padding: 8px 12px;
    cursor: pointer;
}

.dropdown-item:hover {
    background-color: #f0f0f0;
}

.settings-wrapper {
    display: flex;
    gap: 15px;
    margin-right: 15px;
}

.settings-item {
    display: flex;
    align-items: center;
    gap: 5px;
}

.settings-item label {
    font-size: 14px;
    color: #666;
}

.listings-select {
    border: 1px solid #ccc;
    border-radius: 4px;
    padding: 5px 10px;
    font-size: 14px;
    background-color: white;
    cursor: pointer;
    min-width: 70px;
}

.listings-select:focus {
    outline: none;
    border-color: #4285f4;
}

.action-button {
    height: 32px; /* Match input height */
    padding: 0 15px;
    background-color: #f0f0f0;
    border: 1px solid #ccc;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    transition: background-color 0.3s;
    flex-shrink: 0;
}

.action-button:hover {
    background-color: #e0e0e0;
}

.collapse-button-placeholder {
    display: inline-block;
    width: 24px; /* Same as collapse-button padding + font-size roughly */
    height: 24px;
}

.no-price-data {
    text-align: center;
    padding: 20px;
    color: #999;
    background: #fafafa;
    border-radius: 4px;
    margin-top: 10px;
}

.no-price-info-cell {
    text-align: center;
    color: #999;
}

.price-table {
    width: 100%;
    border-collapse: collapse;
    table-layout: fixed; /* 确保列宽固定 */
}

.price-table th,
.price-table td {
    padding: 12px;
    text-align: left;
    border-bottom: 1px solid #eee;
}

.main-row td {
    border-top: none;
    border-bottom: none;
}

.price-table th {
    background: #fafafa;
    font-weight: 500;
    color: #666;
}

.lower-price {
    color: #ff4444;
    font-weight: 500;
}

.no-data,
.loading-state {
    text-align: center;
    padding: 40px;
    color: #666;
    background: #f9f9f9;
    border-radius: 8px;
}

.refresh-section {
    display: flex;
    align-items: center;
    gap: 15px;
}

.refresh-button {
    padding: 8px 20px;
    background-color: #4285f4;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.3s;
}

.refresh-button:hover:not(:disabled) {
    background-color: #3367d6;
}

.refresh-button:disabled {
    background-color: #ccc;
    cursor: not-allowed;
}

.last-update {
    color: #666;
    font-size: 14px;
}

.subscribe-link {
    color: #4285f4;
    text-decoration: none;
    font-weight: 500;
}

.subscribe-link:hover {
    text-decoration: underline;
}

.item-name-cell {
    display: flex;
    align-items: center;
    gap: 8px;
}

.collapse-button {
    background: none;
    border: none;
    cursor: pointer;
    padding: 4px;
    color: #666;
    font-size: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: color 0.3s;
}

.collapse-button:hover {
    color: #4285f4;
}

.expanded-row {
    background-color: #f8f9fa;
}

.expanded-row td:first-child {
    padding-left: 40px;
}

/* Loading animation */
.loader {
  width: 50px;
  aspect-ratio: 1;
  border-radius: 50%;
  background: #000000;
  -webkit-mask: radial-gradient(circle closest-side at 50% 40%,#0000 94%, #000);
  transform-origin: 50% 40%;
  /* 基础动画移除，由table-loading .loader处理 */
  margin: 0 auto;
}

.action-button .loader {
  width: 20px;
  margin: 0 auto;
  display: block;
  animation: loaderRotate 1s infinite linear; /* 按钮加载器只需要旋转动画 */
}

.action-button {
  min-width: 100px; /* 保持按钮宽度一致 */
}

.table-loading {
  position: absolute;
  display: flex;
  justify-content: center;
  align-items: center;
  top: 60px; /* group-header高度 + padding */
  left: 20px;
  right: 20px;
  bottom: 20px;
  background-color: rgba(255, 255, 255, 0.7); /* 半透明背景 */
  z-index: 10;
  backdrop-filter: blur(3px); /* 添加背景模糊效果 */
  animation: fadeInOut 0.8s ease-in-out; /* 浅入浅出动画 */
}

/* 简化的加载动画 - 只保留旋转 */
@keyframes loaderRotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 蒙版淡入淡出效果 */
@keyframes fadeInOut {
  0% { opacity: 0; backdrop-filter: blur(0px); }
  100% { opacity: 1; backdrop-filter: blur(3px); }
}

/* 加载器的呼吸效果 - 仅透明度变化 */
@keyframes loaderPulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

.table-loading .loader {
  /* 分开应用不同的动画属性，避免冲突 */
  animation: loaderRotate 1s infinite linear;
  animation-name: loaderRotate, loaderPulse;
  animation-duration: 1s, 2s;
  animation-timing-function: linear, ease-in-out;
  animation-iteration-count: infinite, infinite;
}

.table-hidden {
  /* 不使用 opacity，因为我们使用模糊效果替代 */
  pointer-events: none; /* 防止在加载时点击表格 */
}
</style>