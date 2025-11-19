<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { 
  PlusOutlined, 
  ReloadOutlined, 
  SettingOutlined, 
  CopyOutlined,
  ExperimentOutlined,
  BarChartOutlined
} from '@ant-design/icons-vue'
import { 
  getConfigList,
  getSchoolList,
  createConfig,
  updateConfig,
  deleteConfig,
  testConnection,
  updateConfigStatus,
  getConfigTemplate,
  batchConfigBySchools,
  copyConfigToSchool,
  getConfigStats,
  type HikvisionConfig,
  type School,
  type BatchConfigRequest,
  type CopyConfigRequest,
  type ConfigStats
} from '@/api/modules/hikvisionConfig'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const configs = ref<HikvisionConfig[]>([])
const schools = ref<School[]>([])
const showModal = ref(false)
const showBatchModal = ref(false)
const showCopyModal = ref(false)
const showStatsModal = ref(false)
const modalLoading = ref(false)
const editingConfig = ref<HikvisionConfig | null>(null)
const stats = ref<ConfigStats>()

const form = reactive<Partial<HikvisionConfig>>({
  schoolId: undefined,
  deviceIp: '192.168.1.100',
  devicePort: 8000,
  sdkVersion: '6.1.9.47',
  deviceCodePrefix: '001',
  protocolType: 'TCP',
  username: 'admin',
  password: '',
  heartbeatEnabled: true,
  heartbeatInterval: 30,
  sdkTimeout: 5000,
  maxRetryCount: 3,
  faceRecognitionEnabled: true,
  cardRecognitionEnabled: true,
  temperatureDetectionEnabled: false,
  syncInterval: 60,
  syncEnabled: true,
  status: 1
})

const batchForm = reactive<BatchConfigRequest>({
  schoolIds: [],
  template: {}
})

const copyForm = reactive<CopyConfigRequest>({
  sourceSchoolId: 0,
  targetSchoolId: 0
})

// 新增：批量模板输入字符串
const batchTemplateInput = ref<string>('')

const columns = [
  { title: '学校', dataIndex: 'schoolId', key: 'schoolId', width: 150 },
  { title: '设备IP', dataIndex: 'deviceIp', key: 'deviceIp', width: 120 },
  { title: '端口', dataIndex: 'devicePort', key: 'devicePort', width: 80 },
  { title: 'SDK版本', dataIndex: 'sdkVersion', key: 'sdkVersion', width: 100 },
  { title: '协议', dataIndex: 'protocolType', key: 'protocolType', width: 80 },
  { title: '心跳检测', dataIndex: 'heartbeatEnabled', key: 'heartbeatEnabled', width: 100 },
  { title: '人脸识别', dataIndex: 'faceRecognitionEnabled', key: 'faceRecognitionEnabled', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '更新时间', dataIndex: 'updatedAt', key: 'updatedAt', width: 150 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' }
]

const protocolOptions = [
  { label: 'TCP', value: 'TCP' },
  { label: 'UDP', value: 'UDP' },
  { label: 'HTTP', value: 'HTTP' },
  { label: 'HTTPS', value: 'HTTPS' }
]

const schoolOptions = computed(() => 
  schools.value.map(school => ({
    label: school.name,
    value: school.id
  }))
)

const getSchoolName = (schoolId: number) => {
  const school = schools.value.find(s => s.id === schoolId)
  return school?.name || `学校ID: ${schoolId}`
}

const loadConfigs = async () => {
  loading.value = true
  try {
    const data = await getConfigList()
    configs.value = data || []
  } catch (e) {
    message.error('加载配置列表失败')
    configs.value = []
  } finally {
    loading.value = false
  }
}

const loadSchools = async () => {
  try {
    const data = await getSchoolList({ size: 1000 })
    // 统一兼容：分页结构 { total, records } 或直接数组
    schools.value = Array.isArray(data) ? data : (data?.records ?? [])
  } catch (e) {
    message.error('加载学校列表失败')
    schools.value = []
  }
}

const loadStats = async () => {
  try {
    stats.value = await getConfigStats()
  } catch (e) {
    message.error('加载统计信息失败')
  }
}

const showAddModal = async () => {
  editingConfig.value = null
  await resetForm()
  showModal.value = true
}

const showEditModal = (config: HikvisionConfig) => {
  editingConfig.value = config
  Object.assign(form, config)
  showModal.value = true
}

const resetForm = async () => {
  try {
    const template = await getConfigTemplate()
    Object.assign(form, template)
  } catch (e) {
    Object.assign(form, {
      schoolId: undefined,
      deviceIp: '192.168.1.100',
      devicePort: 8000,
      sdkVersion: '6.1.9.47',
      deviceCodePrefix: '001',
      protocolType: 'TCP',
      username: 'admin',
      password: '',
      heartbeatEnabled: true,
      heartbeatInterval: 30,
      sdkTimeout: 5000,
      maxRetryCount: 3,
      faceRecognitionEnabled: true,
      cardRecognitionEnabled: true,
      temperatureDetectionEnabled: false,
      syncInterval: 60,
      syncEnabled: true,
      status: 1
    })
  }
}

const handleSubmit = async () => {
  if (!form.schoolId) {
    message.error('请选择学校')
    return
  }
  
  modalLoading.value = true
  try {
    if (editingConfig.value) {
      await updateConfig(editingConfig.value.id!, form)
      message.success('更新配置成功')
    } else {
      await createConfig(form as Omit<HikvisionConfig, 'id' | 'createdAt' | 'updatedAt'>)
      message.success('创建配置成功')
    }
    showModal.value = false
    await loadConfigs()
  } catch (e) {
    message.error(editingConfig.value ? '更新配置失败' : '创建配置失败')
  } finally {
    modalLoading.value = false
  }
}

const handleDelete = (config: HikvisionConfig) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除 "${getSchoolName(config.schoolId)}" 的海康配置吗？`,
    onOk: async () => {
      try {
        await deleteConfig(config.id!)
        message.success('删除成功')
        await loadConfigs()
      } catch (e) {
        message.error('删除失败')
      }
    }
  })
}

const handleTestConnection = async (config: HikvisionConfig) => {
  try {
    const result = await testConnection(config.id!)
    if (result.success) {
      message.success(`连接测试成功，响应时间: ${result.responseTime}ms`)
    } else {
      message.error(`连接测试失败: ${result.message}`)
    }
  } catch (e) {
    message.error('连接测试失败')
  }
}

const toggleConfigStatus = async (config: HikvisionConfig) => {
  try {
    const newStatus = config.status === 1 ? 0 : 1
    await updateConfigStatus(config.id!, newStatus)
    message.success(newStatus === 1 ? '配置已启用' : '配置已禁用')
    await loadConfigs()
  } catch (e) {
    message.error('操作失败')
  }
}

const showBatchConfigModal = async () => {
  batchForm.schoolIds = []
  batchForm.template = {}
  batchTemplateInput.value = ''
  modalLoading.value = true
  try {
    const template = await getConfigTemplate()
    const prefill = {
      deviceIp: template.deviceIp,
      devicePort: template.devicePort,
      username: template.username,
      password: template.password,
      protocolType: template.protocolType,
      heartbeatEnabled: template.heartbeatEnabled,
      heartbeatInterval: template.heartbeatInterval,
      sdkTimeout: template.sdkTimeout,
      maxRetryCount: template.maxRetryCount,
      faceRecognitionEnabled: template.faceRecognitionEnabled,
      cardRecognitionEnabled: template.cardRecognitionEnabled,
      temperatureDetectionEnabled: template.temperatureDetectionEnabled,
      syncEnabled: template.syncEnabled,
      syncInterval: template.syncInterval
    }
    batchTemplateInput.value = JSON.stringify(prefill, null, 2)
  } catch (e) {
    // ignore
  } finally {
    modalLoading.value = false
    showBatchModal.value = true
  }
}

const handleBatchConfig = async () => {
  if (batchForm.schoolIds.length === 0) {
    message.error('请选择至少一个学校')
    return
  }

  // 如果用户输入了模板字符串，尝试解析为对象
  if (batchTemplateInput.value && batchTemplateInput.value.trim().length > 0) {
    try {
      const parsed = JSON.parse(batchTemplateInput.value)
      batchForm.template = parsed
    } catch (err) {
      message.error('配置模板JSON格式不正确')
      return
    }
  }
  
  modalLoading.value = true
  try {
    await batchConfigBySchools(batchForm)
    message.success(`成功为 ${batchForm.schoolIds.length} 个学校配置参数`)
    showBatchModal.value = false
    await loadConfigs()
  } catch (e) {
    message.error('批量配置失败')
  } finally {
    modalLoading.value = false
  }
}

const showCopyConfigModal = () => {
  copyForm.sourceSchoolId = 0
  copyForm.targetSchoolId = 0
  showCopyModal.value = true
}

const handleCopyConfig = async () => {
  if (!copyForm.sourceSchoolId || !copyForm.targetSchoolId) {
    message.error('请选择源学校和目标学校')
    return
  }
  
  if (copyForm.sourceSchoolId === copyForm.targetSchoolId) {
    message.error('源学校和目标学校不能相同')
    return
  }
  
  modalLoading.value = true
  try {
    await copyConfigToSchool(copyForm)
    message.success('配置复制成功')
    showCopyModal.value = false
    await loadConfigs()
  } catch (e) {
    message.error('配置复制失败')
  } finally {
    modalLoading.value = false
  }
}

const showStatsModalHandler = async () => {
  await loadStats()
  showStatsModal.value = true
}

const getStatusText = (status: number) => {
  return status === 1 ? '启用' : '禁用'
}

const getStatusColor = (status: number) => {
  return status === 1 ? 'green' : 'red'
}

onMounted(async () => {
  await Promise.all([loadConfigs(), loadSchools()])
})
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="海康设备配置管理" @back="() => router.push('/attendance/devices')" />

    <a-card>
      <div style="margin-bottom: 16px;">
        <a-space>
          <a-button type="primary" @click="showAddModal">
            <template #icon>
              <plus-outlined />
            </template>
            添加配置
          </a-button>
          <a-button @click="showBatchConfigModal">
            <template #icon>
              <setting-outlined />
            </template>
            批量配置
          </a-button>
          <a-button @click="showCopyConfigModal">
            <template #icon>
              <copy-outlined />
            </template>
            复制配置
          </a-button>
          <a-button @click="showStatsModalHandler">
            <template #icon>
              <bar-chart-outlined />
            </template>
            统计信息
          </a-button>
          <a-button @click="loadConfigs">
            <template #icon>
              <reload-outlined />
            </template>
            刷新
          </a-button>
        </a-space>
      </div>

      <a-table 
        :columns="columns" 
        :data-source="configs" 
        :loading="loading"
        row-key="id"
        :pagination="{ pageSize: 10 }"
        :scroll="{ x: 1200 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'schoolId'">
            {{ getSchoolName(record.schoolId) }}
          </template>
          <template v-else-if="column.key === 'heartbeatEnabled'">
            <a-tag :color="record.heartbeatEnabled ? 'green' : 'red'">
              {{ record.heartbeatEnabled ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'faceRecognitionEnabled'">
            <a-tag :color="record.faceRecognitionEnabled ? 'blue' : 'default'">
              {{ record.faceRecognitionEnabled ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'updatedAt'">
            {{ record.updatedAt ? new Date(record.updatedAt).toLocaleString() : '-' }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="showEditModal(record)">编辑</a-button>
              <a-button 
                size="small" 
                @click="handleTestConnection(record)"
              >
                <template #icon>
                  <experiment-outlined />
                </template>
                测试
              </a-button>
              <a-button 
                size="small" 
                :type="record.status === 1 ? 'default' : 'primary'"
                @click="toggleConfigStatus(record)"
              >
                {{ record.status === 1 ? '禁用' : '启用' }}
              </a-button>
              <a-button size="small" danger @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 添加/编辑配置弹窗 -->
    <a-modal
      v-model:open="showModal"
      :title="editingConfig ? '编辑海康配置' : '添加海康配置'"
      :confirm-loading="modalLoading"
      @ok="handleSubmit"
      @cancel="showModal = false"
      width="800px"
    >
      <a-form layout="vertical" style="margin-top: 16px;">
        <a-form-item label="学校" required>
          <a-select 
            v-model:value="form.schoolId" 
            :options="schoolOptions"
            placeholder="请选择学校"
            show-search
            :filter-option="(input, option) => 
              option?.label?.toLowerCase().includes(input.toLowerCase())
            "
          />
        </a-form-item>

        <a-divider>网络配置</a-divider>
        <a-row :gutter="16">
          <a-col :span="16">
            <a-form-item label="设备IP地址" required>
              <a-input v-model:value="form.deviceIp" placeholder="192.168.1.100" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="端口" required>
              <a-input-number 
                v-model:value="form.devicePort" 
                :min="1" 
                :max="65535" 
                style="width: 100%" 
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="通讯协议">
              <a-select v-model:value="form.protocolType" :options="protocolOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="SDK版本">
              <a-input v-model:value="form.sdkVersion" placeholder="6.1.9.47" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider>认证配置</a-divider>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="用户名" required>
              <a-input v-model:value="form.username" placeholder="admin" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="密码" required>
              <a-input-password v-model:value="form.password" placeholder="请输入密码" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="设备编号前缀">
          <a-input v-model:value="form.deviceCodePrefix" placeholder="001" />
        </a-form-item>

        <a-divider>心跳检测</a-divider>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="启用心跳检测">
              <a-switch v-model:checked="form.heartbeatEnabled" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="心跳间隔(秒)">
              <a-input-number 
                v-model:value="form.heartbeatInterval" 
                :min="10" 
                :max="300" 
                style="width: 100%" 
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider>超时配置</a-divider>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="SDK超时(毫秒)">
              <a-input-number 
                v-model:value="form.sdkTimeout" 
                :min="1000" 
                :max="30000" 
                style="width: 100%" 
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="最大重试次数">
              <a-input-number 
                v-model:value="form.maxRetryCount" 
                :min="1" 
                :max="10" 
                style="width: 100%" 
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider>功能开关</a-divider>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="人脸识别">
              <a-switch v-model:checked="form.faceRecognitionEnabled" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="刷卡识别">
              <a-switch v-model:checked="form.cardRecognitionEnabled" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="体温检测">
              <a-switch v-model:checked="form.temperatureDetectionEnabled" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider>数据同步</a-divider>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="启用自动同步">
              <a-switch v-model:checked="form.syncEnabled" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="同步间隔(秒)">
              <a-input-number 
                v-model:value="form.syncInterval" 
                :min="30" 
                :max="3600" 
                style="width: 100%" 
              />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <!-- 批量配置弹窗 -->
    <a-modal
      v-model:open="showBatchModal"
      title="批量配置学校设备参数"
      :confirm-loading="modalLoading"
      @ok="handleBatchConfig"
      @cancel="showBatchModal = false"
      width="600px"
    >
      <a-form layout="vertical" style="margin-top: 16px;">
        <a-form-item label="选择学校" required>
          <a-select 
            v-model:value="batchForm.schoolIds"
            :options="schoolOptions"
            mode="multiple"
            placeholder="请选择要配置的学校"
            show-search
            :filter-option="(input, option) => 
              option?.label?.toLowerCase().includes(input.toLowerCase())
            "
          />
        </a-form-item>

        <a-form-item label="配置模板（JSON）">
          <a-textarea 
            v-model:value="batchTemplateInput" 
            :rows="8" 
            placeholder='请输入配置参数，例如：
{\n  "deviceIp": "192.168.1.100",\n  "devicePort": 8000,\n  "username": "admin",\n  "password": "12345"\n}'
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 复制配置弹窗 -->
    <a-modal
      v-model:open="showCopyModal"
      title="复制配置到其他学校"
      :confirm-loading="modalLoading"
      @ok="handleCopyConfig"
      @cancel="showCopyModal = false"
      width="500px"
    >
      <a-form layout="vertical" style="margin-top: 16px;">
        <a-form-item label="源学校" required>
          <a-select 
            v-model:value="copyForm.sourceSchoolId"
            :options="schoolOptions"
            placeholder="请选择源学校"
            show-search
            :filter-option="(input, option) => 
              option?.label?.toLowerCase().includes(input.toLowerCase())
            "
          />
        </a-form-item>

        <a-form-item label="目标学校" required>
          <a-select 
            v-model:value="copyForm.targetSchoolId"
            :options="schoolOptions"
            placeholder="请选择目标学校"
            show-search
            :filter-option="(input, option) => 
              option?.label?.toLowerCase().includes(input.toLowerCase())
            "
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 统计信息弹窗 -->
    <a-modal
      v-model:open="showStatsModal"
      title="配置统计信息"
      :footer="null"
      width="400px"
    >
      <div v-if="stats" style="margin-top: 16px;">
        <a-descriptions :column="1" bordered>
          <a-descriptions-item label="总配置数">
            {{ stats.totalConfigs }}
          </a-descriptions-item>
          <a-descriptions-item label="启用配置">
            <a-tag color="green">{{ stats.activeConfigs }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="禁用配置">
            <a-tag color="red">{{ stats.inactiveConfigs }}</a-tag>
          </a-descriptions-item>
        </a-descriptions>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.ant-divider {
  margin: 16px 0 12px 0;
  font-weight: 500;
}
</style>