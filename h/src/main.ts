import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import './style.css'
import { usePermStore } from '@/stores/perm'
import permDirective from '@/directives/perm'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(Antd)
app.directive('perm', permDirective)

async function bootstrap() {
  // 移除开发环境自动登录，完全由登录页手动触发

  const perm = usePermStore()
  try {
    // 若当前在登录页，则不加载权限，避免触发 /auth/me
    const isLoginRoute = router.currentRoute.value?.path === '/login'
    if (!isLoginRoute) {
      await perm.load()
    }
  } catch (e) {
    // 权限加载失败不阻塞应用，但后续守卫会限制访问
    console.warn('Load permissions failed:', e)
  }
  app.mount('#app')
}

bootstrap()
