import type { DirectiveBinding } from 'vue'
import { usePermStore } from '@/stores/perm'

export default {
  mounted(el: HTMLElement, binding: DirectiveBinding<string>) {
    const store = usePermStore()
    const code = binding.value
    const show = store.hasPerm(code)
    if (!show) {
      el.style.display = 'none'
    }
  },
  updated(el: HTMLElement, binding: DirectiveBinding<string>) {
    const store = usePermStore()
    const code = binding.value
    const show = store.hasPerm(code)
    el.style.display = show ? '' : 'none'
  }
}