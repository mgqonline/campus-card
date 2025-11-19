/// <reference types="vite/client" />

// 解决 IDE 对 .vue 导入的类型解析问题
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}