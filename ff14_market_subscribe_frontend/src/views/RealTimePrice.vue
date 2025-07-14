<template>
    <div class="realtime-price-container">
        <div class="nav-bar">
            <router-link to="/" class="nav-item">首页</router-link>
            <router-link to="/subscribe" class="nav-item">物品订阅</router-link>
            <router-link to="/realtime" class="nav-item active">实时物价</router-link>
        </div>
        <div class="content">
            <div class="header">
                <h2 class="page-title">实时物价</h2>
                <div class="refresh-section">
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
                            <button class="action-button" @click="refreshGroup(subscribeGroup.id)">
                                拉取分组物价
                            </button>
                        </div>
                        <table class="price-table">
                            <thead>
                                <tr>
                                    <th>物品名称</th>
                                    <th>所在区服</th>
                                    <th>单价</th>
                                    <th>数量</th>
                                    <th>手续费</th>
                                    <th>总价</th>
                                    <th>品质</th>
                                    <th>雇员名称</th>
                                    <th>生产者</th>
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
                                            <td colspan="8" class="no-price-info-cell">-</td>
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

const COOLDOWN_KEY = 'price_refresh_cooldown';
const LAST_UPDATE_KEY = 'price_last_update';

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
                ElMessage.success('世界已自动保存，正在刷新价格...');
                await refreshGroup(groupId);
            } catch (error: any) {
                console.error('自动保存世界失败:', error);
                ElMessage.error(error.response?.data?.message || '自动保存世界失败');
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

        const refreshGroup = async (groupId: number, showMessage = true) => {
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
            toggleItemPrices,
            isExpanded,
            fetchPriceData,
            formatTime,
            formatPrice,
            refreshGroup,
            // new methods
            searchWorlds,
            selectWorld,
            showWorldDropdown,
            hideDropdown
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
    border: 1px solid #ebeef5;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 20px;
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
</style>