import type { PublicConfig } from '@/api/auth'
import { getPublicConfig } from '@/api/auth'
import { defineStore } from 'pinia'
import { ref } from 'vue'

// InitializeStatus
const initialConfigState: PublicConfig = {
  enableMobileRegister: false,
  version: '',
  year: '',
  allowUserRegister: false,
  mobileAreaList: [],
  beianIcpNum: '',
  beianGaNum: '',
  sm2PublicKey: '',
  name: import.meta.env.VITE_APP_TITLE,
}

export const useConfigStore = defineStore(
  'config',
  () => {
 // defineGlobalConfiguration
    const config = ref<PublicConfig>({ ...initialConfigState })

    // SettingsConfigurationInfo
    const setConfig = (val: PublicConfig) => {
      config.value = val
    }

    // GetPublicConfiguration
    const fetchPublicConfig = async () => {
      try {
        const configData = await getPublicConfig()
        console.log(configData)

        setConfig(configData)
        return configData
      }
      catch (error) {
        console.error('GetPublicConfigurationfailed:', error)
        throw error
      }
    }

    // ResetConfiguration
    const resetConfig = () => {
      config.value = { ...initialConfigState }
    }

    return {
      config,
      setConfig,
      fetchPublicConfig,
      resetConfig,
    }
  },
  {
    persist: {
      key: 'config',
      serializer: {
        serialize: state => JSON.stringify(state.config),
        deserialize: value => ({ config: JSON.parse(value) }),
      },
    },
  },
)
