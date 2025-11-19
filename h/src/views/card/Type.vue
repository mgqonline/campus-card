<template>
  <div class="page">
    <h2>卡种管理</h2>

    <section class="create">
      <div class="row">
        <label>卡种名称</label>
        <input v-model="createForm.name" placeholder="例如：学生卡" />
        <label>说明</label>
        <input v-model="createForm.description" placeholder="可选" />
        <button @click="onCreate" :disabled="creating || !createForm.name">新增</button>
      </div>
    </section>

    <section class="list">
      <div class="summary">卡种列表（{{ list.length }}）</div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th>说明</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in list" :key="item.id">
            <td>{{ item.id }}</td>
            <td>
              <template v-if="editingId === item.id">
                <input v-model="editForm.name" />
              </template>
              <template v-else>{{ item.name }}</template>
            </td>
            <td>
              <template v-if="editingId === item.id">
                <input v-model="editForm.description" />
              </template>
              <template v-else>{{ item.description || '-' }}</template>
            </td>
            <td>
              <template v-if="editingId === item.id">
                <button @click="onSave(item.id)" :disabled="saving">保存</button>
                <button @click="onCancelEdit" :disabled="saving">取消</button>
              </template>
              <template v-else>
                <button @click="onEdit(item)">编辑</button>
                <button @click="onDelete(item.id)" :disabled="deleting">删除</button>
              </template>
            </td>
          </tr>
          <tr v-if="!list.length">
            <td colspan="4" class="empty">暂无卡种</td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { listCardTypes, createCardType, updateCardType, deleteCardType, type CardType } from '@/api/modules/card';

const list = ref<CardType[]>([]);

const createForm = reactive<{ name: string; description?: string }>({ name: '', description: '' });
const creating = ref(false);

const editingId = ref<number | null>(null);
const editForm = reactive<{ name: string; description?: string }>({ name: '', description: '' });
const saving = ref(false);
const deleting = ref(false);

async function load() {
  const res = await listCardTypes();
  list.value = res;
}

async function onCreate() {
  creating.value = true;
  try {
    const r = await createCardType({ name: createForm.name.trim(), description: createForm.description?.trim() });
    if (r.success) {
      list.value.push({ id: r.id || Date.now(), name: createForm.name.trim(), description: createForm.description?.trim() });
      createForm.name = '';
      createForm.description = '';
    }
  } finally {
    creating.value = false;
  }
}

function onEdit(item: CardType) {
  editingId.value = item.id;
  editForm.name = item.name;
  editForm.description = item.description || '';
}

function onCancelEdit() {
  editingId.value = null;
}

async function onSave(id: number) {
  saving.value = true;
  try {
    const r = await updateCardType(id, { name: editForm.name.trim(), description: editForm.description?.trim() });
    if (r.success) {
      const idx = list.value.findIndex((x) => x.id === id);
      if (idx >= 0) list.value[idx] = { id, name: editForm.name.trim(), description: editForm.description?.trim() };
      editingId.value = null;
    }
  } finally {
    saving.value = false;
  }
}

async function onDelete(id: number) {
  if (!confirm('确认删除该卡种吗？')) return;
  deleting.value = true;
  try {
    const r = await deleteCardType(id);
    if (r.success) {
      list.value = list.value.filter((x) => x.id !== id);
    }
  } finally {
    deleting.value = false;
  }
}

onMounted(load);
</script>

<style scoped>
.page { padding: 16px; }
.create, .list { border: 1px solid #eee; padding: 12px; border-radius: 8px; margin-bottom: 16px; }
.row { display: flex; align-items: center; gap: 8px; }
.row > label { color: #666; }
.table { width: 100%; border-collapse: collapse; }
.table th, .table td { border: 1px solid #eee; padding: 8px; text-align: left; }
.empty { text-align: center; color: #999; }
.summary { margin-bottom: 8px; color: #333; }
button { margin-right: 6px; }
</style>