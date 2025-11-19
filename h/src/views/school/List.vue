<template>
  <div>
    <div style="margin-bottom: 16px;">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-input v-model:value="query.name" placeholder="学校名称" allow-clear />
        </a-col>
        <a-col :span="6">
          <a-input v-model:value="query.code" placeholder="学校代码" allow-clear />
        </a-col>
        <a-col :span="6">
          <a-select v-model:value="query.status" placeholder="状态" allow-clear style="width: 100%">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-col>
        <a-col :span="6">
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
            <a-button type="primary" @click="handleAdd">新增学校</a-button>
          </a-space>
        </a-col>
      </a-row>
    </div>

    <a-table 
      :columns="columns" 
      :data-source="dataSource" 
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
      row-key="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '启用' : '禁用' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
            <a-button 
              type="link" 
              size="small" 
              @click="handleToggleStatus(record)"
              :style="{ color: record.status === 1 ? '#ff4d4f' : '#52c41a' }"
            >
              {{ record.status === 1 ? '禁用' : '启用' }}
            </a-button>
            <a-popconfirm
              title="确定要删除这个学校吗？"
              @confirm="handleDelete(record.id)"
              ok-text="确定"
              cancel-text="取消"
            >
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 新增/编辑学校弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑学校' : '新增学校'"
      @ok="handleSubmit"
      @cancel="handleCancel"
      :confirm-loading="submitLoading"
      width="800px"
    >
      <a-form
        ref="formRef"
        :model="form"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="学校名称" name="name">
              <a-input v-model:value="form.name" placeholder="请输入学校名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="学校代码" name="code">
              <a-input v-model:value="form.code" placeholder="请输入学校代码" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="联系电话" name="phone">
              <a-input v-model:value="form.phone" placeholder="请输入联系电话" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="邮箱地址" name="email">
              <a-input v-model:value="form.email" placeholder="请输入邮箱地址" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="校长姓名" name="principal">
              <a-input v-model:value="form.principal" placeholder="请输入校长姓名" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态" name="status">
              <a-select v-model:value="form.status" placeholder="请选择状态">
                <a-select-option :value="1">启用</a-select-option>
                <a-select-option :value="0">禁用</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="学校地址" name="address">
          <a-input v-model:value="form.address" placeholder="请输入学校地址" />
        </a-form-item>
        <a-form-item label="学校简介" name="description">
          <a-textarea 
            v-model:value="form.description" 
            placeholder="请输入学校简介" 
            :rows="4"
            :maxlength="500"
            show-count
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { TableColumnsType, FormInstance } from 'ant-design-vue'
import { schoolApi, type School, type SchoolQuery } from '@/api/modules/school'

// 表格列定义
const columns: TableColumnsType = [
  { title: '学校名称', dataIndex: 'name', key: 'name', width: 200 },
  { title: '学校代码', dataIndex: 'code', key: 'code', width: 120 },
  { title: '联系电话', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '邮箱地址', dataIndex: 'email', key: 'email', width: 180 },
  { title: '校长', dataIndex: 'principal', key: 'principal', width: 100 },
  { title: '地址', dataIndex: 'address', key: 'address', width: 200, ellipsis: true },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 160 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' }
]

// 响应式数据
const loading = ref(false)
const dataSource = ref<School[]>([])
const modalVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

// 查询参数
const query = reactive<SchoolQuery>({
  name: '',
  code: '',
  status: undefined,
  page: 1,
  size: 10
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`
})

// 表单数据
const form = reactive<Partial<School>>({
  name: '',
  code: '',
  address: '',
  phone: '',
  email: '',
  principal: '',
  description: '',
  status: 1
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入学校名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入学校代码', trigger: 'blur' }],
  address: [{ required: true, message: '请输入学校地址', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  principal: [{ required: true, message: '请输入校长姓名', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      ...query,
      page: pagination.current,
      size: pagination.pageSize
    }
    const response = await schoolApi.getList(params)
    dataSource.value = response.list
    pagination.total = response.total
  } catch (error) {
    message.error('获取学校列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  query.name = ''
  query.code = ''
  query.status = undefined
  pagination.current = 1
  fetchData()
}

// 表格变化处理
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

// 新增学校
const handleAdd = () => {
  isEdit.value = false
  modalVisible.value = true
  resetForm()
}

// 编辑学校
const handleEdit = (record: School) => {
  isEdit.value = true
  modalVisible.value = true
  Object.assign(form, record)
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    name: '',
    code: '',
    address: '',
    phone: '',
    email: '',
    principal: '',
    description: '',
    status: 1
  })
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true
    
    if (isEdit.value) {
      await schoolApi.update(form.id!, form)
      message.success('更新学校成功')
    } else {
      await schoolApi.create(form as Omit<School, 'id' | 'createTime' | 'updateTime'>)
      message.success('创建学校成功')
    }
    
    modalVisible.value = false
    fetchData()
  } catch (error) {
    message.error(isEdit.value ? '更新学校失败' : '创建学校失败')
  } finally {
    submitLoading.value = false
  }
}

// 取消操作
const handleCancel = () => {
  modalVisible.value = false
  resetForm()
}

// 切换状态
const handleToggleStatus = async (record: School) => {
  try {
    const newStatus = record.status === 1 ? 0 : 1
    await schoolApi.updateStatus(record.id!, newStatus)
    message.success(`${newStatus === 1 ? '启用' : '禁用'}学校成功`)
    fetchData()
  } catch (error) {
    message.error('操作失败')
  }
}

// 删除学校
const handleDelete = async (id: number) => {
  try {
    await schoolApi.delete(id)
    message.success('删除学校成功')
    fetchData()
  } catch (error) {
    message.error('删除学校失败')
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.ant-table {
  background: #fff;
}
</style>