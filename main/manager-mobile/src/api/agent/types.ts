// Agent listDataType
export interface Agent {
  id: string
  agentName: string
  ttsModelName: string
  ttsVoiceName: string
  llmModelName: string
  vllmModelName: string
  memModelId: string
  systemPrompt: string
  summaryMemory: string | null
  lastConnectedAt: string | null
  deviceCount: number
  tags: Record<string, string>[]
}

// AgentCreateDataType
export interface AgentCreateData {
  agentName: string
}

// AgentdetailsDataType
export interface AgentDetail {
  id: string
  userId: string
  agentCode: string
  agentName: string
  asrModelId: string
  vadModelId: string
  llmModelId: string
  slmModelId: string
  vllmModelId: string
  ttsModelId: string
  ttsVoiceId: string
  memModelId: string
  intentModelId: string
  chatHistoryConf: number
  systemPrompt: string
  summaryMemory: string
  langCode: string
  language: string
  sort: number
  creator: string
  createdAt: string
  updater: string
  updatedAt: string
  ttsLanguage: string
  ttsVolume: number
  ttsRate: number
  ttsPitch: number
  functions: AgentFunction[]
  contextProviders: Providers[]
}

export interface Providers {
  url: string
  headers: Array<{
    key: string
    value: string
  }>
}

export interface AgentFunction {
  id?: string
  agentId?: string
  pluginId: string
  paramInfo: Record<string, string | number | boolean> | null
}

// RoleTemplateDataType
export interface RoleTemplate {
  id: string
  agentCode: string
  agentName: string
  asrModelId: string
  vadModelId: string
  llmModelId: string
  vllmModelId: string
  ttsModelId: string
  ttsVoiceId: string
  memModelId: string
  intentModelId: string
  chatHistoryConf: number
  systemPrompt: string
  summaryMemory: string
  langCode: string
  language: string
  sort: number
  creator: string
  createdAt: string
  updater: string
  updatedAt: string
}

// ModelOptionDataType
export interface ModelOption {
  id: string
  modelName: string
}

export interface PluginField {
  key: string
  type: string
  label: string
  default: string
  selected?: boolean
  editing?: boolean
}

export interface PluginDefinition {
  id: string
  modelType: string
  providerCode: string
  name: string
  fields: PluginField[] // Note: the original is a string and must be JSON.parsed first
  sort: number
  updater: string
  updateDate: string
  creator: string
  createDate: string
  [key: string]: any
}
