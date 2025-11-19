<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined, ReloadOutlined, SettingOutlined } from '@ant-design/icons-vue'
import { 
  getDeviceList, 
  registerDevice, 
  updateDevice, 
  deleteDevice, 
  setDeviceOnline, 
  setDeviceOffline,
  type Device 
} from '@/api/modules/attendanceDevice'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const devices = ref<Device[]>([])
const showModal = ref(false)
const modalLoading = ref(false)
const editingDevice = ref<Device | null>(null)

const form = reactive<Device>({
  name: '',
  code: '',
  vendor: 'HIKVISION',
  ip: '',
  port: 80,
  username: 'admin',
  password: '',
  location: '',
  status: 1,
  groupType: 'STUDENT',
  paramJson: ''
})

const columns = [
  { title: '设备名称', dataIndex: 'name', key: 'name' },
  { title: '设备编码', dataIndex: 'code', key: 'code' },
  { title: '厂商', dataIndex: 'vendor', key: 'vendor' },
  { title: 'IP地址', dataIndex: 'ip', key: 'ip' },
  { title: '端口', dataIndex: 'port', key: 'port' },
  { title: '位置', dataIndex: 'location', key: 'location' },
  { title: '分组', dataIndex: 'groupType', key: 'groupType' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '最后心跳', dataIndex: 'lastHeartbeatAt', key: 'lastHeartbeatAt' },
  { title: '操作', key: 'action' }
]

const vendorOptions = [
  { label: '海康威视', value: 'HIKVISION' },
  { label: '大华', value: 'DAHUA' },
  { label: '宇视', value: 'UNIVIEW' },
  { label: '其他', value: 'OTHER' }
]

const groupTypeOptions = [
  { label: '学生考勤', value: 'STUDENT' },
  { label: '教师考勤', value: 'TEACHER' },
  { label: '门禁管理', value: 'ACCESS' }
]

const loadDevices = async () => {
  loading.value = true
  try {
    const raw: any = await getDeviceList()
    // 调试输出，便于确认后端返回结构
    if (import.meta && (import.meta as any).env && (import.meta as any).env.DEV) {
      console.log('[Devices] getDeviceList raw:', raw)
    }
    // 兼容多种返回结构：
    // 1) 直接数组
    // 2) 分页 { records, total }
    // 3) { list, total } 或 { items, count }
    // 4) { data: [] }
    let list: Device[] = []
    if (Array.isArray(raw)) {
      list = raw as Device[]
    } else if (raw && typeof raw === 'object') {
      if (Array.isArray((raw as any).records)) list = (raw as any).records
      else if (Array.isArray((raw as any).list)) list = (raw as any).list
      else if (Array.isArray((raw as any).items)) list = (raw as any).items
      else if (Array.isArray((raw as any).data)) list = (raw as any).data
    }
    devices.value = list
  } catch (e) {
    message.error('加载设备列表失败')
    devices.value = []
  } finally {
    loading.value = false
  }
}

const showAddModal = () => {
  editingDevice.value = null
  resetForm()
  showModal.value = true
}

const showEditModal = (device: Device) => {
  editingDevice.value = device
  Object.assign(form, device)
  showModal.value = true
}

const resetForm = () => {
  Object.assign(form, {
    name: '',
    code: '',
    vendor: 'HIKVISION',
    ip: '',
    port: 80,
    username: 'admin',
    password: '',
    location: '',
    status: 1,
    groupType: 'STUDENT',
    paramJson: ''
  })
}

const handleSubmit = async () => {
  modalLoading.value = true
  try {
    if (editingDevice.value) {
      await updateDevice(editingDevice.value.id!, form)
      message.success('更新设备成功')
    } else {
      await registerDevice(form)
      message.success('注册设备成功')
    }
    showModal.value = false
    await loadDevices()
  } catch (e) {
    message.error(editingDevice.value ? '更新设备失败' : '注册设备失败')
  } finally {
    modalLoading.value = false
  }
}

const handleDelete = (device: Device) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除设备 "${device.name}" 吗？`,
    onOk: async () => {
      try {
        await deleteDevice(device.id!)
        message.success('删除成功')
        await loadDevices()
      } catch (e) {
        message.error('删除失败')
      }
    }
  })
}

const toggleDeviceStatus = async (device: Device) => {
  try {
    if (device.status === 1) {
      await setDeviceOffline(device.id!)
      message.success('设备已设为离线')
    } else {
      await setDeviceOnline(device.id!)
      message.success('设备已设为在线')
    }
    await loadDevices()
  } catch (e) {
    message.error('操作失败')
  }
}

const getStatusText = (status: number) => {
  return status === 1 ? '在线' : '离线'
}

const getStatusColor = (status: number) => {
  return status === 1 ? 'green' : 'red'
}

const getGroupTypeText = (groupType: string) => {
  const option = groupTypeOptions.find(opt => opt.value === groupType)
  return option?.label || groupType
}

onMounted(loadDevices)
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="考勤设备管理" @back="() => router.push('/attendance/stat')" />

    <a-card>
      <div style="margin-bottom: 16px;">
        <a-button type="primary" @click="showAddModal">
          <template #icon>
            <plus-outlined />
          </template>
          添加设备
        </a-button>
        <a-button style="margin-left: 8px;" @click="$router.push('/attendance/hikvision-config')">
          <template #icon>
            <setting-outlined />
          </template>
          海康配置
        </a-button>
        <a-button style="margin-left: 8px;" @click="loadDevices">
          <template #icon>
            <reload-outlined />
          </template>
          刷新
        </a-button>
      </div>

      <a-table 
        :columns="columns" 
        :data-source="devices" 
        :loading="loading"
        row-key="id"
        :pagination="{ pageSize: 10 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'groupType'">
            {{ getGroupTypeText(record.groupType) }}
          </template>
          <template v-else-if="column.key === 'lastHeartbeatAt'">
            {{ record.lastHeartbeatAt ? new Date(record.lastHeartbeatAt).toLocaleString() : '-' }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="showEditModal(record)">编辑</a-button>
              <a-button 
                size="small" 
                :type="record.status === 1 ? 'default' : 'primary'"
                @click="toggleDeviceStatus(record)"
              >
                {{ record.status === 1 ? '设为离线' : '设为在线' }}
              </a-button>
              <a-button size="small" danger @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 添加/编辑设备弹窗 -->
    <a-modal
      v-model:open="showModal"
      :title="editingDevice ? '编辑设备' : '添加设备'"
      :confirm-loading="modalLoading"
      @ok="handleSubmit"
      @cancel="showModal = false"
      width="600px"
    >
      <a-form layout="vertical" style="margin-top: 16px;">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="设备名称" required>
              <a-input v-model:value="form.name" placeholder="请输入设备名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="设备编码" required>
              <a-input v-model:value="form.code" placeholder="请输入设备编码" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="厂商">
              <a-select v-model:value="form.vendor" :options="vendorOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="设备分组">
              <a-select v-model:value="form.groupType" :options="groupTypeOptions" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="16">
            <a-form-item label="IP地址">
              <a-input v-model:value="form.ip" placeholder="192.168.1.100" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="端口">
              <a-input-number v-model:value="form.port" :min="1" :max="65535" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="用户名">
              <a-input v-model:value="form.username" placeholder="admin" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="密码">
              <a-input-password v-model:value="form.password" placeholder="请输入密码" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="安装位置">
          <a-input v-model:value="form.location" placeholder="如：教学楼一楼大厅" />
        </a-form-item>

        <a-form-item label="设备参数（JSON格式）">
          <a-textarea 
            v-model:value="form.paramJson" 
            :rows="3" 
            placeholder='{"timeout": 5000, "retryCount": 3}'
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>