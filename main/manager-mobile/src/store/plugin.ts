import type { AgentFunction, PluginDefinition } from '@/api/agent/types'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const usePluginStore = defineStore(
  'plugin',
  () => {
 // allplugin
    const allFunctions = ref<PluginDefinition[]>([])
 // currentAgent of pluginConfiguration
    const currentFunctions = ref<AgentFunction[]>([])
 // currentEdit of AgentID
    const currentAgentId = ref('')
 // Settingsallplugin
    const setAllFunctions = (functions: PluginDefinition[]) => {
      allFunctions.value = functions
    }
 // SettingscurrentAgent of pluginConfiguration
    const setCurrentFunctions = (functions: AgentFunction[]) => {
      currentFunctions.value = functions
    }
 // SettingscurrentAgentID
    const setCurrentAgentId = (agentId: string) => {
      currentAgentId.value = agentId
    }
 // UpdatepluginConfiguration（Used forSavewhenCall）
    const updateFunctions = (functions: AgentFunction[]) => {
      currentFunctions.value = functions
    }

    // ClearData
    const clear = () => {
      allFunctions.value = []
      currentFunctions.value = []
      currentAgentId.value = ''
    }

    return {
      allFunctions,
      currentFunctions,
      currentAgentId,
      setAllFunctions,
      setCurrentFunctions,
      setCurrentAgentId,
      updateFunctions,
      clear,
    }
  },
  {
    persist: false, // 不持久化，每次进入页面重新加载
  },
)
