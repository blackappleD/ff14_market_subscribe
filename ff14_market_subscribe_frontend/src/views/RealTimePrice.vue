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
                        <template v-if="loading">加载中...</template>
                        <template v-else-if="cooldownTime > 0">
                            {{ cooldownTime }}秒后可刷新
                        </template>
                        <template v-else>刷新数据</template>
                    </button>
                    <span class="last-update" v-if="lastUpdateTime">
                        上次刷新: {{ formatTime(lastUpdateTime) }}
                    </span>
                </div>
            </div>
 
            <!-- 加载状态显示 -->
            <div v-if="isLoading" class="loading-state">
                数据加载中，请稍后...
            </div>

            <!-- 订阅实时价格分组展示 -->
            <template v-else>
                <div v-if="hasSubscriptions">
                    <div v-for="subscribeGroup in priceData" :key="subscribeGroup.id" class="subscribe-group">
                        <h3 class="subescribe-world-title">{{ subscribeGroup.worldName }}</h3>
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
                                    <!-- 主行（默认显示第一条价格信息） -->
                                    <tr class="main-row">
                                        <td>
                                            <div class="item-name-cell">
                                                <button class="collapse-button"
                                                    @click="toggleItemPrices(subscribeGroup.id, item.id)">
                                                    {{ isExpanded(subscribeGroup.id, item.id) ? '▼' : '▶' }}
                                                </button>
                                                {{ item.name }}
                                            </div>
                                        </td>
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
import { defineComponent, ref, onMounted, onUnmounted } from 'vue';
import axios from '@/utils/axios';
import { useRouter } from 'vue-router';

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

interface subscribePriceGroup {
    id: number;
    worldName: string;
    itemPriceGroups: ItemPriceGroup[];
}

const COOLDOWN_KEY = 'price_refresh_cooldown';
const LAST_UPDATE_KEY = 'price_last_update';

export default defineComponent({
    name: 'RealTimePrice',
    setup() {
        const router = useRouter();
        const priceData = ref<subscribePriceGroup[]>([]);
        const loading = ref(false);
        const isLoading = ref(true);
        const hasSubscriptions = ref(false);
        const lastUpdateTime = ref<Date | null>(null);
        const cooldownTime = ref(0);
        let cooldownTimer: number | null = null;
        const expandedItems = ref(new Set<string>());

        // 检查是否在冷却时间内
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

        // 更新冷却时间存储
        const updateCooldownStorage = () => {
            localStorage.setItem(COOLDOWN_KEY, 'true');
            localStorage.setItem(LAST_UPDATE_KEY, Date.now().toString());
        };

        // 检查是否有订阅数据
        const checkSubscriptions = async () => {
            try {
                const response = await axios.get('/ff14/subscribe');
                hasSubscriptions.value = response.data.data && response.data.data.length > 0;
                if (hasSubscriptions.value) {
                    if (!checkCooldown()) {
                        await fetchPriceData();
                    } else {
                        // 如果在冷却时间内，尝试从缓存加载数据
                        const cachedData = localStorage.getItem('price_data');
                        if (cachedData) {
                            priceData.value = JSON.parse(cachedData);
                            const savedLastUpdate = localStorage.getItem(LAST_UPDATE_KEY);
                            if (savedLastUpdate) {
                                lastUpdateTime.value = new Date(parseInt(savedLastUpdate));
                            }
                        }
                    }
                }
            } catch (error: any) {
                console.error('检查订阅失败:', error);
                if (error.response?.status === 401) {
                    router.push('/login');
                } else {
                    alert(error.response?.data?.message || error.response?.data?.data || '检查订阅状态失败，请重试');
                }
            } finally {
                isLoading.value = false;
            }
        };

        // 开始倒计时
        const startCooldown = () => {
            cooldownTime.value = cooldownTime.value || 120;
            if (cooldownTimer) {
                clearInterval(cooldownTimer);
            }
            cooldownTimer = window.setInterval(() => {
                if (cooldownTime.value > 0) {
                    cooldownTime.value--;
                    // 同步更新 localStorage 中的时间
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

        const fetchPriceData = async () => {
            if (loading.value || cooldownTime.value > 0) return;
            try {
                loading.value = true;
                // 增加超时时间为 60 秒，等待接口较长时间响应
                const response = await axios.get('/ff14/price/on_time', { timeout: 120000 });
                priceData.value = response.data.data;
                lastUpdateTime.value = new Date();
                
                // 缓存数据
                localStorage.setItem('price_data', JSON.stringify(response.data.data));
                updateCooldownStorage();
                startCooldown();
            } catch (error: any) {
                console.error('获取价格数据失败:', error);
                if (error.response?.status === 401) {
                    router.push('/login');
                } else {
                    // 对超时错误进行额外判断
                    if (error.code === 'ECONNABORTED') {
                        alert('请求超时，数据加载较慢，请稍后重试');
                    } else {
                        alert(error.response?.data?.message || error.response?.data?.data || '获取数据失败，请重试');
                    }
                }
            } finally {
                loading.value = false;
            }
        };

        onMounted(() => {
            checkSubscriptions();
        });

        onUnmounted(() => {
            if (cooldownTimer) {
                clearInterval(cooldownTimer);
            }
        });

        const formatPrice = (price: number): string => {
            return price.toLocaleString();
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
            fetchPriceData,
            formatPrice,
            formatTime,
            toggleItemPrices,
            isExpanded,
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
    margin-bottom: 30px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    overflow: hidden;
}

.subescribe-world-title {
    padding: 15px 20px;
    background: #f5f5f5;
    margin: 0;
    font-size: 18px;
    color: #333;
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

.no-data {
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

.loading-state {
    text-align: center;
    padding: 40px;
    color: #666;
    background: #f9f9f9;
    border-radius: 8px;
    margin-bottom: 20px;
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