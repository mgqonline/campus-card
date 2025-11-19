import { createApp } from 'vue'
import App from './App.vue'
import { createPinia } from 'pinia'
import router from './router'
import { Icon, Popup, DatePicker } from 'vant'
import 'vant/lib/index.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(Icon)
app.use(Popup)
app.use(DatePicker)
app.mount('#app')
