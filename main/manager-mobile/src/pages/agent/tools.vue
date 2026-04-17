<route lang="jsonc" type="page">
{
  "layout": "default",
  "style": {
    "navigationBarTitleText": "编辑功能",
    "navigationStyle": "custom"
  }
}
</route>

<script lang="ts" setup>
import { useMessage } from 'wot-design-uni'
import { getMcpAddress, getMcpTools } from '@/api/agent/agent'
import { t } from '@/i18n'
import { usePluginStore } from '@/store'

const message = useMessage()
const pluginStore = usePluginStore()

const segmentedList = ref<string[]>([t('agent.tools.notSelected'), t('agent.tools.selected')])
const currentSegmented = ref(t('agent.tools.notSelected'))
const notSelectedList = ref<any[]>([])
const selectedList = ref<any[]>([])

// UsecalculatePropertyfromstoreGetData
const allFunctions = computed(() => pluginStore.allFunctions)
const functions = computed(() => pluginStore.currentFunctions)
const agentId = computed(() => pluginStore.currentAgentId)
const mcpAddress = ref('')
const mcpTools = ref<string[]>([])

// InitializewhenfromLocalStorageLoadMCPAddress
if (uni.getStorageSync(`cachedMcpAddress_${agentId.value}`)) {
  mcpAddress.value = uni.getStorageSync(`cachedMcpAddress_${agentId.value}`)
}

// ParameterEdit
const showParamDialog = ref(false)
const currentFunction = ref<any>(null)
const tempParams = ref<Record<string, any>>({})
const arrayTextCache = ref<Record<string, string>>({})
const jsonTextCache = ref<Record<string, string>>({})

async function mergeFunctions() {
  selectedList.value = functions.value.map((mapping) => {
    const meta = allFunctions.value.find(f => f.id === mapping.pluginId)
    if (!meta) {
      return { id: mapping.pluginId, name: mapping.pluginId, params: {} }
    }

    return {
      id: mapping.pluginId,
      name: meta.name,
      params: mapping.paramInfo || { ...meta.params },
      fieldsMeta: meta.fieldsMeta,
    }
  })
 // of plugin
  notSelectedList.value = allFunctions.value.filter(
    item => !selectedList.value.some(f => f.id === item.id),
  )

  if (agentId.value) {
 // firstGetandShowMCPAddress
    try {
      const address = await getMcpAddress(agentId.value)
      mcpAddress.value = address
 // CachetoLocalStorage，OpenPageShow
      uni.setStorageSync(`cachedMcpAddress_${agentId.value}`, address)
    }
    catch (error) {
      mcpAddress.value = error
      console.error('GetMCPAddressfailed:', error)
    }
 // GetMCPtoollist，UIShow
    try {
      const tools = await getMcpTools(agentId.value)
      mcpTools.value = tools || []
    }
    catch (error) {
      console.error('GetMCPtoollistfailed:', error)
    }
  }
}

// Addplugintoalready
function selectFunction(func: any) {
 // Addtoalreadylist
  selectedList.value = [...selectedList.value, {
    id: func.id,
    name: func.name,
    params: { ...func.params },
    fieldsMeta: func.fieldsMeta,
  }]
 // fromlistinRemove
  notSelectedList.value = notSelectedList.value.filter(
    item => item.id !== func.id,
  )
}

// fromalreadySelectedRemoveplugin
function removeFunction(func: any) {
 // fromalreadylistinRemove
  selectedList.value = selectedList.value.filter(item => item.id !== func.id)
 // Addlist
  const originalFunc = allFunctions.value.find(f => f.id === func.id)
  if (originalFunc) {
    notSelectedList.value.push(originalFunc)
  }
}

// EditpluginParameter
function editFunction(func: any) {
  currentFunction.value = func
 // directlyUsecurrentFunction of Parameter
  tempParams.value = { ...func.params }
 // InitializeTextCache
  if (func.fieldsMeta) {
    func.fieldsMeta.forEach((field: any) => {
      if (field.type === 'array') {
        const value = tempParams.value[field.key]
        arrayTextCache.value[field.key] = Array.isArray(value)
          ? value.join('\n')
          : value || ''
      }
      else if (field.type === 'json') {
        const value = tempParams.value[field.key]
        try {
          jsonTextCache.value[field.key] = JSON.stringify(value || {}, null, 2)
        }
        catch {
          jsonTextCache.value[field.key] = '{}'
        }
      }
    })
  }

  showParamDialog.value = true
}

// ProcessParameterchange - real-timeSave
function handleParamChange(key: string, value: any, field: any) {
  tempParams.value[key] = value
 // real-timeUpdateto selectedList
  if (currentFunction.value) {
    const index = selectedList.value.findIndex(
      f => f.id === currentFunction.value.id,
    )
    if (index >= 0) {
      selectedList.value[index].params = { ...tempParams.value }
    }
  }
}

// ProcessArrayTypeParameterchange - real-timeSave
function handleArrayChange(key: string, value: string, field: any) {
  arrayTextCache.value[key] = value
 // Convert toArrayStorage
  const arrayValue = value.split('\n').filter(Boolean)
  tempParams.value[key] = arrayValue
 // real-timeUpdateto selectedList
  if (currentFunction.value) {
    const index = selectedList.value.findIndex(
      f => f.id === currentFunction.value.id,
    )
    if (index >= 0) {
      selectedList.value[index].params = { ...tempParams.value }
    }
  }
}

// ProcessJSONTypeParameterchange - real-timeSave
function handleJsonChange(key: string, value: string, field: any) {
  jsonTextCache.value[key] = value
  try {
    const jsonValue = JSON.parse(value)
    tempParams.value[key] = jsonValue
 // real-timeUpdateto selectedList
    if (currentFunction.value) {
      const index = selectedList.value.findIndex(
        f => f.id === currentFunction.value.id,
      )
      if (index >= 0) {
        selectedList.value[index].params = { ...tempParams.value }
      }
    }
  }
  catch {
    message.alert(t('agent.tools.jsonFormatError'))
  }
}

// CloseParameterEditDialog
function closeParamEdit() {
  showParamDialog.value = false
  tempParams.value = {}
  arrayTextCache.value = {}
  jsonTextCache.value = {}
}

// BackPreviousandUpdateConfiguration
function goBack() {
  uni.navigateBack()
}

// CopyMCPAddress
function copyMcpAddress() {
  if (!mcpAddress.value) {
    message.alert(t('agent.tools.noMcpAddressToCopy'))
    return
  }

  uni.setClipboardData({
    data: mcpAddress.value,
    showToast: false,
    success: () => {
      message.alert(t('agent.tools.mcpAddressCopied'))
    },
    fail: () => {
      message.alert(t('agent.tools.copyFailed'))
    },
  })
}

// ParameterField of Function
function getFieldDisplayValue(field: any, value: any) {
  if (field.type === 'array') {
    return Array.isArray(value) ? value.join('\n') : value || ''
  }
  return value || ''
}

// Field
function getFieldRemark(field: any) {
  let description = field.label || ''
  if (field.default) {
    description += `（${t('agent.tools.defaultValue')}：${field.default}）`
  }
  return description
}

// listen toalreadylistchange，real-timeUpdatepluginConfiguration，UserBackButtonConfiguration
watch(() => selectedList.value, (newSelectedList) => {
  const finalFunctions = newSelectedList.map(f => ({
    pluginId: f.id,
    paramInfo: f.params,
  }))
  pluginStore.updateFunctions(finalFunctions)
})

onMounted(async () => {
 // directlyfromstoreGetDataandand
  await mergeFunctions()
})
</script>

<template>
  <view class="h-screen flex flex-col bg-[#f5f7fb]">
    <!-- Navigation -->
    <wd-navbar
      title=""
      safe-area-inset-top
      left-arrow
      :bordered="false"
      @click-left="goBack"
    >
      <template #left>
        <wd-icon name="arrow-left" size="18" />
      </template>
    </wd-navbar>

    <!-- Contentarea -->
    <scroll-view
      scroll-y
      class="box-border flex-1 bg-transparent px-[20rpx]"
      :style="{ height: 'calc(100vh - 120rpx)' }"
      :scroll-with-animation="true"
    >
      <!-- pluginarea -->
      <view class="mt-[20rpx] flex flex-1 flex-col">
        <view class="text-[32rpx] text-[#333] font-medium">
          {{ t('agent.tools.builtInPlugins') }}
        </view>
        <view
          class="mt-[20rpx] box-border flex flex-1 flex-col rounded-[10rpx] bg-white p-[20rpx]"
        >
          <!-- -->
          <wd-segmented
            v-model:value="currentSegmented"
            :options="segmentedList"
          />

          <!-- pluginlist -->
          <view class="mt-[20rpx] flex-1 overflow-hidden">
            <!-- plugin -->
            <scroll-view
              v-if="currentSegmented === t('agent.tools.notSelected')"
              class="max-h-[600rpx] bg-transparent"
              scroll-y
            >
              <view
                v-if="notSelectedList.length === 0"
                class="h-[400rpx] flex items-center justify-center"
              >
                <wd-status-tip image="content" :tip="t('agent.tools.noMorePlugins')" />
              </view>
              <view v-else class="p-[20rpx] space-y-[20rpx]">
                <view
                  v-for="func in notSelectedList"
                  :key="func.id"
                  class="flex items-center justify-between border border-[#e9ecef] rounded-[10rpx] bg-[#f8f9fa] p-[20rpx]"
                  @click="selectFunction(func)"
                >
                  <view class="flex-1">
                    <view
                      class="mb-[10rpx] text-[30rpx] text-[#333] font-medium"
                    >
                      {{ func.name }}
                    </view>
                    <view class="text-[24rpx] text-[#666]">
                      {{ func.providerCode }}
                    </view>
                  </view>
                  <view
                    class="h-[60rpx] w-[60rpx] flex items-center justify-center rounded-full bg-[#1677ff]"
                  >
                    <text class="text-[36rpx] text-white">
                      +
                    </text>
                  </view>
                </view>
              </view>
            </scroll-view>

            <!-- alreadyplugin -->
            <scroll-view v-else class="max-h-[600rpx] bg-transparent" scroll-y>
              <view
                v-if="selectedList.length === 0"
                class="h-[400rpx] flex items-center justify-center"
              >
                <wd-status-tip image="content" :tip="t('agent.tools.pleaseSelectPlugin')" />
              </view>
              <view v-else class="p-[20rpx] space-y-[20rpx]">
                <view
                  v-for="func in selectedList"
                  :key="func.id"
                  class="border border-[#d4edff] rounded-[10rpx] bg-[#f0f7ff] p-[20rpx]"
                >
                  <view class="flex items-center justify-between">
                    <view class="flex-1" @click="editFunction(func)">
                      <view
                        class="mb-[10rpx] text-[30rpx] text-[#333] font-medium"
                      >
                        {{ func.name }}
                      </view>
                      <view class="text-[24rpx] text-[#1677ff]">
                        {{ t('agent.tools.clickToConfigure') }}
                      </view>
                    </view>
                    <view class="flex space-x-[20rpx]">
                      <!-- ConfigurationButton -->
                      <view
                        class="h-[60rpx] w-[60rpx] flex items-center justify-center rounded-full bg-[#1677ff]"
                        @click="editFunction(func)"
                      >
                        <text class="text-[24rpx] text-white">
                          ⚙
                        </text>
                      </view>
                      <!-- RemoveButton -->
                      <view
                        class="h-[60rpx] w-[60rpx] flex items-center justify-center rounded-full bg-[#ff4757]"
                        @click="removeFunction(func)"
                      >
                        <text class="text-[32rpx] text-white">
                          ×
                        </text>
                      </view>
                    </view>
                  </view>
                </view>
              </view>
            </scroll-view>
          </view>
        </view>
      </view>

      <!-- MCParea -->
      <view class="mt-[20rpx] flex flex-1 flex-col">
        <view class="text-[32rpx] text-[#333] font-medium">
          {{ t('agent.tools.mcpEndpoint') }}
        </view>
        <view
          class="mt-[20rpx] box-border flex flex-1 flex-col rounded-[10rpx] bg-white p-[20rpx]"
        >
          <view class="flex items-center justify-between text-[24rpx]">
            <input
              v-model="mcpAddress"
              type="text"
              disabled
              class="flex-1 rounded-[10rpx] bg-[#f5f7fb] p-[20rpx]"
            >
            <view
              class="ml-[20rpx] h-[70rpx] flex items-center justify-center rounded-[10rpx] bg-[#1677ff] px-[20rpx] text-[24rpx] text-white"
              @click="copyMcpAddress"
            >
              {{ t('agent.tools.copy') }}
            </view>
          </view>
          <!-- toollist -->
          <view class="mt-[20rpx] flex-1 overflow-hidden">
            <scroll-view class="max-h-[600rpx] bg-transparent" scroll-y>
              <view
                v-if="mcpTools && mcpTools.length === 0"
                class="h-[400rpx] flex items-center justify-center"
              >
                <wd-status-tip image="content" :tip="t('agent.tools.noTools')" />
              </view>
              <view v-else class="p-[20rpx]">
                <view class="flex flex-wrap">
                  <view
                    v-for="tool in mcpTools"
                    :key="tool"
                    class="mb-[20rpx] mr-[20rpx] rounded-[10rpx] bg-[#f5f7fb] p-[20rpx]"
                  >
                    {{ tool }}
                  </view>
                </view>
              </view>
            </scroll-view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- ParameterEditDialog -->
    <wd-action-sheet
      v-model="showParamDialog"
      :title="`${t('agent.tools.parameterConfig')} - ${currentFunction?.name || ''}`"
      custom-header-class="h-[75vh]"
      @close="closeParamEdit"
    >
      <scroll-view
        scroll-y
        class="bg-[#f5f7fb]"
        :style="{ height: 'calc(75vh - 60rpx)' }"
      >
        <view class="p-[30rpx] pb-[40rpx]">
          <!-- ParameterNotice -->
          <view
            v-if="
              !currentFunction?.fieldsMeta
                || currentFunction.fieldsMeta.length === 0
            "
            class="h-[400rpx] flex items-center justify-center"
          >
            <text class="text-[28rpx] text-[#999]">
              {{ currentFunction?.name }} {{ t('agent.tools.noParamsNeeded') }}
            </text>
          </view>

          <!-- ParameterForm - card -->
          <view v-else class="flex flex-col gap-[24rpx]">
            <view
              v-for="field in currentFunction.fieldsMeta"
              :key="field.key"
              class="border border-[#eeeeee] rounded-[20rpx] bg-white p-[30rpx]"
              style="box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);"
            >
              <!-- FieldInfo -->
              <view class="mb-[24rpx]">
                <text class="mb-[8rpx] block text-[32rpx] text-[#232338] font-medium">
                  {{ field.label }}
                </text>
                <text v-if="getFieldRemark(field)" class="block text-[24rpx] text-[#65686f] leading-[1.5]">
                  {{ getFieldRemark(field) }}
                </text>
              </view>

              <!-- -->
              <view>
                <!-- StringType -->
                <input
                  v-if="field.type === 'string'"
                  v-model="tempParams[field.key]"
                  class="box-border h-[80rpx] w-full border border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[16rpx_20rpx] text-[28rpx] text-[#232338] focus:border-[#336cff] focus:bg-white placeholder:text-[#9d9ea3]"
                  type="text"
                  :placeholder="`${t('agent.tools.pleaseInput')}${field.label}`"
                  @input="
                    handleParamChange(field.key, $event.detail.value, field)
                  "
                >

                <!-- ArrayType -->
                <view v-else-if="field.type === 'array'">
                  <text class="mb-[16rpx] block text-[24rpx] text-[#65686f]">
                    {{ t('agent.tools.eachLineOneItem') }}
                  </text>
                  <textarea
                    v-model="arrayTextCache[field.key]"
                    class="box-border min-h-[200rpx] w-full border border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[20rpx] text-[26rpx] text-[#232338] leading-[1.6] focus:border-[#336cff] focus:bg-white placeholder:text-[#9d9ea3]"
                    :placeholder="`${t('agent.tools.pleaseInput')}${field.label}，${t('agent.tools.eachLineOneItem')}`"
                    @input="
                      handleArrayChange(field.key, $event.detail.value, field)
                    "
                  />
                </view>

                <!-- JSONType -->
                <view v-else-if="field.type === 'json'">
                  <text class="mb-[16rpx] block text-[24rpx] text-[#65686f]">
                    {{ t('agent.tools.pleaseInputValidJson') }}
                  </text>
                  <textarea
                    v-model="jsonTextCache[field.key]"
                    class="box-border min-h-[300rpx] w-full border border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[20rpx] text-[26rpx] text-[#232338] leading-[1.6] font-mono focus:border-[#336cff] focus:bg-white placeholder:text-[#9d9ea3]"
                    :placeholder="t('agent.tools.pleaseInputValidJson')"
                    @blur="
                      handleJsonChange(field.key, $event.detail.value, field)
                    "
                  />
                </view>

                <!-- NumberType -->
                <input
                  v-else-if="field.type === 'number'"
                  v-model="tempParams[field.key]"
                  class="box-border h-[80rpx] w-full border border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[16rpx_20rpx] text-[28rpx] text-[#232338] focus:border-[#336cff] focus:bg-white placeholder:text-[#9d9ea3]"
                  type="number"
                  :placeholder="`${t('agent.tools.pleaseInput')}${field.label}`"
                  @input="
                    handleParamChange(
                      field.key,
                      Number($event.detail.value),
                      field,
                    )
                  "
                >

                <!-- BooleanType -->
                <view
                  v-else-if="field.type === 'boolean' || field.type === 'bool'"
                  class="flex items-center justify-between py-[20rpx]"
                >
                  <view class="flex-1">
                    <text class="mb-[8rpx] block text-[28rpx] text-[#232338]">
                      {{ t('agent.tools.enableFunction') }}
                    </text>
                    <text class="block text-[24rpx] text-[#65686f]">
                      {{ t('agent.tools.toggleFunction') }}
                    </text>
                  </view>
                  <switch
                    :checked="tempParams[field.key]"
                    @change="
                      handleParamChange(field.key, $event.detail.value, field)
                    "
                  />
                </view>

                <!-- DefaultStringType -->
                <input
                  v-else
                  v-model="tempParams[field.key]"
                  class="box-border h-[80rpx] w-full border border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[16rpx_20rpx] text-[28rpx] text-[#232338] focus:border-[#336cff] focus:bg-white placeholder:text-[#9d9ea3]"
                  type="text"
                  :placeholder="`${t('agent.tools.pleaseInput')}${field.label}`"
                  @input="
                    handleParamChange(field.key, $event.detail.value, field)
                  "
                >
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </wd-action-sheet>
  </view>
</template>

<style scoped lang="scss">
::v-deep .wd-action-sheet__header {
  padding-right: 30rpx;
}
</style>
