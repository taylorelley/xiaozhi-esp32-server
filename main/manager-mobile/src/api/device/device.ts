import type { Device, FirmwareType } from './types'
import { http } from '@/http/request/alova'

/**
 * GetDeviceTypelist
 */
export function getFirmwareTypes() {
  return http.Get<FirmwareType[]>('/admin/dict/data/type/FIRMWARE_TYPE')
}

/**
 * GetBindDevice list
 * @param agentId AgentID
 */
export function getBindDevices(agentId: string) {
  return http.Get<Device[]>(`/device/bind/${agentId}`, {
    meta: {
      ignoreAuth: false,
      toast: false,
    },
    cacheFor: {
      expire: 0,
    },
  })
}

/**
 * AddDevice
 * @param agentId AgentID
 * @param code Captcha
 */
export function bindDevice(agentId: string, code: string) {
  return http.Post(`/device/bind/${agentId}/${code}`, null)
}

/** * manualAddDevice * @param agentId AgentID * @param board DeviceType * @param appVersion Firmware * @param macAddress MACAddress */
export function bindDeviceManual(data: {
  agentId: string
  board: string
  appVersion: string
  macAddress: string
}) {
  return http.Post('/device/manual-add', data)
}

/** * SettingsDeviceOTA * @param deviceId DeviceID (MACAddress) * @param autoUpdate Whether to 0|1 */
export function updateDeviceAutoUpdate(deviceId: string, autoUpdate: number) {
  return http.Put(`/device/update/${deviceId}`, {
    autoUpdate,
  })
}

/**
 * UnbindDevice
 * @param deviceId DeviceID (MACAddress)
 */
export function unbindDevice(deviceId: string) {
  return http.Post('/device/unbind', {
    deviceId,
  })
}
