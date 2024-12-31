import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

const app = createApp(App)

// 添加全局错误处理
app.config.errorHandler = (err, vm, info) => {
  console.error('Global error:', err)
  console.error('Error info:', info)
}

// 添加路由错误处理
router.onError((error) => {
  console.error('Router error:', error)
})

app.use(store).use(router).mount('#app') 