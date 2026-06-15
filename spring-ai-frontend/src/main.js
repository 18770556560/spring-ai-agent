import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createHead } from '@vueuse/head'
// 全局样式由 App.vue 提供

const app = createApp(App)
const head = createHead()

app.use(router)
app.use(head)
app.mount('#app')
