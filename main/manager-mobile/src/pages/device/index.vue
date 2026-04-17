<script lang="ts" setup>
import type { Device, FirmwareType } from '@/api/device'
import { computed, onMounted, ref } from 'vue'
import { useMessage } from 'wot-design-uni'
import { bindDevice, bindDeviceManual, getBindDevices, getFirmwareTypes, unbindDevice, updateDeviceAutoUpdate } from '@/api/device'
import { t } from '@/i18n'
import { toast } from '@/utils/toast'

defineOptions({
  name: 'DeviceManage',
})

const props = withDefaults(defineProps<Props>(), {
  agentId: 'default',
})

const actions = [
  { key: 'code', name: t('manualAddDeviceDialog.bindWithCode') },
  { key: 'manual', name: t('manualAddDeviceDialog.title') },
]

// props
interface Props {
  agentId?: string
}

// Getscreen boundary toSecurityregion distance
let safeAreaInsets: any
let systemInfo: any

// #ifdef MP-WEIXIN
systemInfo = uni.getWindowInfo()
safeAreaInsets = systemInfo.safeArea
  ? {
      top: systemInfo.safeArea.top,
      right: systemInfo.windowWidth - systemInfo.safeArea.right,
      bottom: systemInfo.windowHeight - systemInfo.safeArea.bottom,
      left: systemInfo.safeArea.left,
    }
  : null
// #endif

// #ifndef MP-WEIXIN
systemInfo = uni.getSystemInfoSync()
safeAreaInsets = systemInfo.safeAreaInsets
// #endif

// DeviceData
const deviceList = ref<Device[]>([])
const firmwareTypes = ref<FirmwareType[]>([])
const loading = ref(false)
const isBindDevice = ref(false)

// manualBindDialog
const isManualBindDialog = ref(false)
const manualBindForm = ref({
  board: '',
  appVersion: '',
  macAddress: '',
})

// FormValidateErrorNotice
const formErrors = ref({
  board: '',
  appVersion: '',
  macAddress: '',
})

// MACAddressthenValidate
const macRegex = /^(?:[0-9A-F]{2}[:-]){5}[0-9A-F]{2}$/i

function selectBindMode(row) {
  if (row.item.key === 'code') {
    openBindDialog()
  }
  else if (row.item.key === 'manual') {
 // OpenDialogResetFormandErrorNotice
    manualBindForm.value = {
      board: '',
      appVersion: '',
      macAddress: '',
    }
    formErrors.value = {
      board: '',
      appVersion: '',
      macAddress: '',
    }
    isManualBindDialog.value = true
  }
}

// MessageComponent
const message = useMessage()

// Use of AgentID
const currentAgentId = computed(() => {
  return props.agentId
})

// GetDevice list
async function loadDeviceList() {
  try {
 // CheckWhether tohascurrentSelected of Agent
    if (!currentAgentId.value) {
      deviceList.value = []
      return
    }

    loading.value = true
    const response = await getBindDevices(currentAgentId.value)
    deviceList.value = response || []
  }
  catch (error) {
    console.error('GetDevice listfailed:', error)
    deviceList.value = []
  }
  finally {
    loading.value = false
  }
}

// Component of RefreshMethod
async function refresh() {
  await loadDeviceList()
}

// GetDeviceTypeName
function getDeviceTypeName(boardKey: string): string {
  const firmwareType = firmwareTypes.value.find(type => type.key === boardKey)
  return firmwareType?.name || boardKey
}

// Formatwhen
function formatTime(timeStr: string) {
  if (!timeStr)
    return t('device.neverConnected')
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60000)
    return t('device.justNow')
  if (diff < 3600000)
    return t('device.minutesAgo', { minutes: Math.floor(diff / 60000) })
  if (diff < 86400000)
    return t('device.hoursAgo', { hours: Math.floor(diff / 3600000) })
  if (diff < 604800000)
    return t('device.daysAgo', { days: Math.floor(diff / 86400000) })

  return date.toLocaleDateString()
}

// SwitchOTAUpdate
async function toggleAutoUpdate(device: Device) {
  try {
    const newStatus = device.autoUpdate === 1 ? 0 : 1
    await updateDeviceAutoUpdate(device.id, newStatus)
    device.autoUpdate = newStatus
    toast.success(newStatus === 1 ? t('device.otaAutoUpdateEnabled') : t('device.otaAutoUpdateDisabled'))
  }
  catch (error: any) {
    console.error('UpdateDeviceOTAStatusfailed:', error)
    toast.error(t('device.operationFailed'))
  }
}

// UnbindDevice
async function handleUnbindDevice(device: Device) {
  try {
    await unbindDevice(device.id)
    await loadDeviceList()
    toast.success(t('device.deviceUnbound'))
  }
  catch (error: any) {
    console.error('UnbindDevicefailed:', error)
    toast.error(t('device.unbindFailed'))
  }
}

// ConfirmUnbindDevice
function confirmUnbindDevice(device: Device) {
  message.confirm({
    title: t('device.unbindDevice'),
    msg: t('device.confirmUnbindDevice', { macAddress: device.macAddress }),
    confirmButtonText: t('device.confirmUnbind'),
    cancelButtonText: t('device.cancel'),
  }).then(() => {
    handleUnbindDevice(device)
  }).catch(() => {
    // UserCancel
  })
}

// BindnewDevice
async function handleBindDevice(code: string) {
  try {
    if (!currentAgentId.value) {
      toast.error(t('device.pleaseSelectAgent'))
      return
    }

    await bindDevice(currentAgentId.value, code.trim())
    await loadDeviceList()
    toast.success(t('device.deviceBindSuccess'))
  }
  catch (error: any) {
    console.error('BindDevicefailed:', error)
    const errorMessage = error?.message || t('device.bindFailed')
    toast.error(errorMessage)
  }
}

// OpenBindDeviceDialog
function openBindDialog() {
  message
    .prompt({
      title: t('device.bindDevice'),
      inputPlaceholder: t('device.enterDeviceCode'),
      inputValue: '',
      inputPattern: /^\d{6}$/,
      confirmButtonText: t('device.bindNow'),
      cancelButtonText: t('device.cancel'),
    })
    .then(async (result: any) => {
      if (result.value && String(result.value).trim()) {
        await handleBindDevice(String(result.value).trim())
      }
    })
    .catch(() => {
      // UserCancelAction
    })
}

// manualBindDevice
async function handleManualBind() {
  try {
 // firstValidateForm
    const isValid = validateForm()
    if (!isValid) {
      return
    }

    if (!currentAgentId.value) {
      toast.error(t('device.pleaseSelectAgent'))
      return
    }

    await bindDeviceManual({
      agentId: currentAgentId.value,
      board: manualBindForm.value.board,
      appVersion: manualBindForm.value.appVersion,
      macAddress: manualBindForm.value.macAddress,
    })
    await loadDeviceList()
    toast.success(t('manualAddDeviceDialog.addSuccess'))
    isManualBindDialog.value = false
 // ResetFormandErrorNotice
    manualBindForm.value = {
      board: '',
      appVersion: '',
      macAddress: '',
    }
    formErrors.value = {
      board: '',
      appVersion: '',
      macAddress: '',
    }
  }
  catch (error: any) {
    const errorMessage = error?.message || t('manualAddDeviceDialog.addFailed')
    toast.error(errorMessage)
  }
}

// ValidatesingleField
function validateField(field: string) {
  switch (field) {
    case 'board':
      if (!manualBindForm.value.board) {
        formErrors.value.board = t('manualAddDeviceDialog.deviceTypePlaceholder')
      }
      else {
        formErrors.value.board = ''
      }
      break
    case 'appVersion':
      if (!manualBindForm.value.appVersion) {
        formErrors.value.appVersion = t('manualAddDeviceDialog.firmwareVersionPlaceholder')
      }
      else {
        formErrors.value.appVersion = ''
      }
      break
    case 'macAddress':
      if (!manualBindForm.value.macAddress) {
        formErrors.value.macAddress = t('manualAddDeviceDialog.macAddressPlaceholder')
      }
      else if (!macRegex.test(manualBindForm.value.macAddress)) {
        formErrors.value.macAddress = t('manualAddDeviceDialog.invalidMacAddress')
      }
      else {
        formErrors.value.macAddress = ''
      }
      break
  }
}

// ClearFieldErrorNotice
function clearFieldError(field: string) {
  formErrors.value[field] = ''
}

// ProcessSelectchange
function handlePickerChange() {
  clearFieldError('board')
}

// ValidateForm
function validateForm(): boolean {
  let isValid = true
 // ValidateDeviceType
  if (!manualBindForm.value.board) {
    formErrors.value.board = t('manualAddDeviceDialog.deviceTypePlaceholder')
    isValid = false
  }
  else {
    formErrors.value.board = ''
  }
 // ValidateFirmware
  if (!manualBindForm.value.appVersion) {
    formErrors.value.appVersion = t('manualAddDeviceDialog.firmwareVersionPlaceholder')
    isValid = false
  }
  else {
    formErrors.value.appVersion = ''
  }
 // ValidateMACAddress
  if (!manualBindForm.value.macAddress) {
    formErrors.value.macAddress = t('manualAddDeviceDialog.macAddressPlaceholder')
    isValid = false
  }
  else if (!macRegex.test(manualBindForm.value.macAddress)) {
    formErrors.value.macAddress = t('manualAddDeviceDialog.invalidMacAddress')
    isValid = false
  }
  else {
    formErrors.value.macAddress = ''
  }

  return isValid
}

// GetDeviceTypelist
async function loadFirmwareTypes() {
  try {
    const response = await getFirmwareTypes()
    firmwareTypes.value = response
  }
  catch (error) {
    console.error('GetDeviceTypefailed:', error)
  }
}

onMounted(async () => {
 // AgentalreadyisDefault

  loadFirmwareTypes()
  loadDeviceList()
})

// MethodComponent
defineExpose({
  refresh,
})
</script>

<template>
  <view class="device-container" style="background: #f5f7fb; min-height: 100%;">
    <!-- LoadStatus -->
    <view v-if="loading && deviceList.length === 0" class="loading-container">
      <wd-loading color="#336cff" />
      <text class="loading-text">
        {{ t('device.loading') }}
      </text>
    </view>

    <!-- Device list -->
    <view v-else-if="deviceList.length > 0" class="device-list">
      <!-- Devicecardlist -->
      <view class="box-border flex flex-col gap-[24rpx] p-[20rpx]">
        <view v-for="device in deviceList" :key="device.id">
          <wd-swipe-action>
            <view class="cursor-pointer bg-[#fbfbfb] p-[32rpx] transition-all duration-200 active:bg-[#f8f9fa]">
              <view class="flex items-start justify-between">
                <view class="flex-1">
                  <view class="mb-[16rpx] flex items-center justify-between">
                    <text class="max-w-[60%] break-all text-[32rpx] text-[#232338] font-semibold">
                      {{ getDeviceTypeName(device.board) }}
                    </text>
                  </view>

                  <view class="mb-[20rpx]">
                    <text class="mb-[12rpx] block text-[28rpx] text-[#65686f] leading-[1.4]">
                      {{ t('device.macAddress') }}：{{ device.macAddress }}
                    </text>
                    <text class="mb-[12rpx] block text-[28rpx] text-[#65686f] leading-[1.4]">
                      {{ t('device.firmwareVersion') }}：{{ device.appVersion }}
                    </text>
                    <text class="block text-[28rpx] text-[#65686f] leading-[1.4]">
                      {{ t('device.lastConnection') }}：{{ formatTime(device.lastConnectedAt) }}
                    </text>
                  </view>

                  <view class="flex items-center justify-between border-[1rpx] border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[16rpx_20rpx]">
                    <text class="text-[28rpx] text-[#232338] font-medium">
                      {{ t('device.otaUpdate') }}
                    </text>
                    <wd-switch
                      :model-value="device.autoUpdate === 1"
                      size="24"
                      @change="toggleAutoUpdate(device)"
                    />
                  </view>
                </view>
              </view>
            </view>

            <template #right>
              <view class="h-full flex">
                <view
                  class="h-full min-w-[120rpx] flex items-center justify-center bg-[#ff4d4f] p-x-[32rpx] text-[28rpx] text-white font-medium"
                  @click.stop="confirmUnbindDevice(device)"
                >
                  <wd-icon name="delete" />
                  <text>{{ t('device.unbind') }}</text>
                </view>
              </view>
            </template>
          </wd-swipe-action>
        </view>
      </view>
    </view>

    <!-- Status -->
    <view v-else-if="!loading" class="empty-container">
      <view class="flex flex-col items-center justify-center p-[100rpx_40rpx] text-center">
        <wd-icon name="phone" custom-class="text-[120rpx] text-[#d9d9d9] mb-[32rpx]" />
        <text class="mb-[16rpx] text-[32rpx] text-[#666666] font-medium">
          {{ t('device.noDevice') }}
        </text>
        <text class="text-[26rpx] text-[#999999] leading-[1.5]">
          {{ t('device.clickToBindFirstDevice') }}
        </text>
      </view>
    </view>

    <!-- FAB BindDeviceButton -->
    <wd-fab type="primary" size="small" icon="add" :draggable="true" :expandable="false" @click="isBindDevice = true" />

    <!-- MessageBox Component -->
    <wd-message-box />
    <wd-action-sheet v-model="isBindDevice" :actions="actions" @close="isBindDevice = false" @select="selectBindMode" />

    <!-- manualBindDeviceDialog -->
    <wd-popup v-model="isManualBindDialog" position="bottom" :close-on-click-modal="false" custom-style="border-radius: 24rpx 24rpx 0 0;">
      <view class="manual-bind-dialog">
        <view class="dialog-header">
          <text class="dialog-title">
            {{ t('manualAddDeviceDialog.title') }}
          </text>
          <wd-icon name="close" size="20" @click="isManualBindDialog = false" />
        </view>

        <view class="dialog-content">
          <view class="form-item">
            <text class="form-label">
              {{ t('manualAddDeviceDialog.deviceType') }}
              <text class="required">
                *
              </text>
            </text>
            <wd-picker
              v-model="manualBindForm.board"
              class="custom-wd-picker"
              :columns="firmwareTypes.map(item => ({ value: item.key, label: item.name }))"
              :placeholder="t('manualAddDeviceDialog.deviceTypePlaceholder')"
              :cancel-button-text="t('common.cancel')"
              :confirm-button-text="t('common.confirm')"
              @confirm="handlePickerChange"
            />
            <text v-if="formErrors.board" class="error-text">
              {{ formErrors.board }}
            </text>
          </view>

          <view class="form-item">
            <text class="form-label">
              {{ t('manualAddDeviceDialog.firmwareVersion') }}
              <text class="required">
                *
              </text>
            </text>
            <wd-input
              v-model="manualBindForm.appVersion"
              :placeholder="t('manualAddDeviceDialog.firmwareVersionPlaceholder')"
              @input="clearFieldError('appVersion')"
              @blur="validateField('appVersion')"
            />
            <text v-if="formErrors.appVersion" class="error-text">
              {{ formErrors.appVersion }}
            </text>
          </view>

          <view class="form-item">
            <text class="form-label">
              {{ t('manualAddDeviceDialog.macAddress') }}
              <text class="required">
                *
              </text>
            </text>
            <wd-input
              v-model="manualBindForm.macAddress"
              :placeholder="t('manualAddDeviceDialog.macAddressPlaceholder')"
              @input="validateField('macAddress')"
              @blur="validateField('macAddress')"
            />
            <text v-if="formErrors.macAddress" class="error-text">
              {{ formErrors.macAddress }}
            </text>
          </view>
        </view>

        <view class="dialog-footer">
          <wd-button block type="primary" @click="handleManualBind">
            {{ t('manualAddDeviceDialog.confirm') }}
          </wd-button>
        </view>
      </view>
    </wd-popup>
  </view>
</template>

<style scoped>
.device-container {
  position: relative;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 40rpx;
}

.loading-text {
  margin-top: 20rpx;
  font-size: 28rpx;
  color: #666666;
}

:deep(.wd-swipe-action) {
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid #eeeeee;
}
::v-deep .wd-action-sheet__popup,
::v-deep .wd-popup {
  z-index: 100 !important;
}
.custom-wd-picker ::v-deep .wd-picker__cell {
  padding-left: 0 !important;
}

:deep(.wd-icon) {
  font-size: 32rpx;
}

.manual-bind-dialog {
  padding: 32rpx;
  background: #ffffff;
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32rpx;
}

.dialog-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #232338;
}

.dialog-content {
  margin-bottom: 32rpx;
}

.form-item {
  margin-bottom: 24rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #65686f;
  margin-bottom: 12rpx;
}

.required {
  color: #ff4d4f;
  margin-left: 4rpx;
}

.error-text {
  display: block;
  font-size: 24rpx;
  color: #ff4d4f;
  margin-top: 8rpx;
}

.dialog-footer {
  padding-top: 16rpx;
}
</style>
