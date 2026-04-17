<script lang="ts" setup>
import type { AgentDetail, ModelOption, PluginDefinition, RoleTemplate } from '@/api/agent/types'
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { getAgentDetail, getAgentTags, getAllLanguage, getModelOptions, getPluginFunctions, getRoleTemplates, updateAgent, updateAgentTags } from '@/api/agent/agent'
import { t } from '@/i18n'
import { usePluginStore, useProvider, useSpeedPitch } from '@/store'
import { toast } from '@/utils/toast'

defineOptions({
  name: 'AgentEdit',
})

const props = withDefaults(defineProps<Props>(), {
  agentId: '',
})

// ComponentParameter
interface Props {
  agentId?: string
}

const agentId = computed(() => props.agentId)

// FormData
const formData = ref<Partial<AgentDetail>>({
  agentName: '',
  systemPrompt: '',
  summaryMemory: '',
  vadModelId: '',
  asrModelId: '',
  llmModelId: '',
  slmModelId: '',
  vllmModelId: '',
  intentModelId: '',
  memModelId: '',
  ttsModelId: '',
  ttsVoiceId: '',
  ttsLanguage: '',
  ttsVolume: 0,
  ttsRate: 0,
  ttsPitch: 0,
})

// ShowNameData
const displayNames = ref({
// ShowNameData
  vad: t('agent.pleaseSelect'),
  asr: t('agent.pleaseSelect'),
  llm: t('agent.pleaseSelect'),
  slm: t('agent.pleaseSelect'),
  vllm: t('agent.pleaseSelect'),
  intent: t('agent.pleaseSelect'),
  memory: t('agent.pleaseSelect'),
  tts: t('agent.pleaseSelect'),
  voiceprint: t('agent.pleaseSelect'),
  report: t('agent.pleaseSelect'),
  language: t('agent.pleaseSelect'),
})

// RoleTemplateData
const roleTemplates = ref<RoleTemplate[]>([])
const selectedTemplateId = ref('')

// LoadStatus
const loading = ref(false)
const saving = ref(false)

// ModelOptionData
const modelOptions = ref<{
  [key: string]: ModelOption[]
}>({
  VAD: [],
  ASR: [],
  LLM: [],
  VLLM: [],
  Intent: [],
  Memory: [],
  TTS: [],
})

// VoiceOptionData
const voiceOptions = ref([])
// Save of VoiceInfo
const voiceDetails = ref({})

// modeOptionData
const reportOptions = [
  { name: t('agent.reportText'), value: 1 },
  { name: t('agent.reportTextVoice'), value: 2 },
]

// SelectShowStatus
const pickerShow = ref<{
  [key: string]: boolean
}>({
  vad: false,
  asr: false,
  llm: false,
  slm: false,
  vllm: false,
  intent: false,
  memory: false,
  tts: false,
  voiceprint: false,
  language: false,
  report: false,
})

const allFunctions = ref<PluginDefinition[]>([])
const dynamicTags = ref([])
const inputValue = ref('')
const inputVisible = ref(false)
const languageOptions = ref([])
const isVisibleReport = ref(false)
const tempSummaryMemory = ref('')

// AudioPlay
const audioRef = ref<UniApp.InnerAudioContext | null>(null)
const playingVoiceId = ref<string>('')

// Usepluginstore
const pluginStore = usePluginStore()
const speedPitchStore = useSpeedPitch()
const providerStore = useProvider()

// tabs
const tabList = [
  {
    label: 'Role Config',
    value: 'home',
    icon: '/static/tabbar/robot.png',
    activeIcon: '/static/tabbar/robot_activate.png',
  },
  {
    label: 'Device Management',
    value: 'category',
    icon: '/static/tabbar/device.png',
    activeIcon: '/static/tabbar/device_activate.png',
  },
  {
    label: 'Chat History',
    value: 'settings',
    icon: '/static/tabbar/chat.png',
    activeIcon: '/static/tabbar/chat_activate.png',
  },
  {
    label: 'Voiceprint Management',
    value: 'profile',
    icon: '/static/tabbar/voiceprint.png',
    activeIcon: '/static/tabbar/voiceprint_activate.png',
  },
]
function handleCloseTag(id: string) {
  dynamicTags.value = dynamicTags.value.filter(tag => tag.id !== id)
}

function showInput() {
  inputVisible.value = true
}

function handleInputConfirm() {
  if (inputValue.value) {
    dynamicTags.value.push({ id: new Date().getTime(), tagName: inputValue.value.trim() })
    inputValue.value = ''
  }
  inputVisible.value = false
}

// Whether toDisableHistoryinput
const isMemoryDisabled = computed(() => formData.value.memModelId !== 'Memory_mem_local_short')

// OpenEditDialog
function openContextProviderDialog() {
  uni.navigateTo({
    url: '/pages/agent/provider',
  })
}

function handleRegulate() {
  uni.navigateTo({
    url: '/pages/agent/speedPitch',
  })
}

// LoadAgentdetails
async function loadAgentDetail() {
  if (!agentId.value)
    return

  try {
    loading.value = true
    tempSummaryMemory.value = ''
    const detail = await getAgentDetail(agentId.value)
    formData.value = { ...detail }
 // Updatepluginstore
    pluginStore.setCurrentAgentId(agentId.value)
    pluginStore.setCurrentFunctions(detail.functions || [])
 // Update
    speedPitchStore.updateSpeedPitch({
      ttsVolume: detail.ttsVolume || 0,
      ttsRate: detail.ttsRate || 0,
      ttsPitch: detail.ttsPitch || 0,
    })
 // LoadConfiguration
    providerStore.updateProviders(detail.contextProviders || [])
 // IfhasTTSModel，LoadcorrespondingVoiceOption
    if (detail.ttsModelId) {
      await fetchAllLanguag(detail.ttsModelId)
    }
 // waitModelOptionLoadDoneafterUpdateShowName
    await nextTick()
    updateDisplayNames()
  }
  catch (error) {
    console.error('LoadAgentdetailsfailed:', error)
    toast.error(t('agent.loadFail'))
  }
  finally {
    loading.value = false
  }
}

// GetVoiceShowName
function getVoiceDisplayName(ttsVoiceId: string) {
  if (!ttsVoiceId)
    return 'Please select'

  console.log('=== Voice ===')
  console.log('currentVoiceID:', ttsVoiceId)
  console.log('currentTTSModel:', formData.value.ttsModelId)
  console.log('VoiceOption:', voiceOptions.value)
 // firstdirectlyfromVoiceOptioninID
  const voice = voiceOptions.value.find(v => v.id === ttsVoiceId)
  if (voice) {
    console.log('directlysuccessful:', voice)
    return voice.name
  }
 // Ifto，
  if (voiceOptions.value.length > 0) {
    console.log('directlyfailed，')
 // Create：voice1 → 1Voice，voice2 → 2Voice
    const indexMap = {
      voice1: 0,
      voice2: 1,
      voice3: 2,
      voice4: 3,
      voice5: 4,
    }

    const index = indexMap[ttsVoiceId]
    if (index !== undefined && voiceOptions.value[index]) {
      const mappedVoice = voiceOptions.value[index]
      console.log(`: ${ttsVoiceId} → index ${index} → ${mappedVoice.name}`)
      return mappedVoice.name
    }
  }

  console.log('allmodefailed，BackID:', ttsVoiceId)
  return ttsVoiceId
}

// UpdateShowName
function updateDisplayNames() {
  if (!formData.value)
    return

  displayNames.value.vad = getModelDisplayName('VAD', formData.value.vadModelId)
  displayNames.value.asr = getModelDisplayName('ASR', formData.value.asrModelId)
  displayNames.value.llm = getModelDisplayName('LLM', formData.value.llmModelId)
  displayNames.value.slm = getModelDisplayName('LLM', formData.value.slmModelId)
  displayNames.value.vllm = getModelDisplayName('VLLM', formData.value.vllmModelId)
  displayNames.value.intent = getModelDisplayName('Intent', formData.value.intentModelId)
  displayNames.value.memory = getModelDisplayName('Memory', formData.value.memModelId)
  displayNames.value.tts = getModelDisplayName('TTS', formData.value.ttsModelId)
 // RoleVoiceProcess
  displayNames.value.report = reportOptions.find(item => item.value === formData.value.chatHistoryConf)?.name

  isVisibleReport.value = formData.value.memModelId !== 'Memory_nomem'

  console.log('VoiceShowName:', displayNames.value.voiceprint)
}

// LoadRoleTemplate
async function loadRoleTemplates() {
  try {
    const templates = await getRoleTemplates()
    roleTemplates.value = templates
  }
  catch (error) {
    console.error('LoadRoleTemplatefailed:', error)
  }
}

// LoadModelOption
async function loadModelOptions() {
  const modelTypes = ['VAD', 'ASR', 'LLM', 'VLLM', 'Intent', 'Memory', 'TTS']

  try {
    await Promise.all(
      modelTypes?.map(async (type) => {
        console.log(`LoadModelType: ${type}`)
        const options = await getModelOptions(type)
        modelOptions.value[type] = options
        console.log(`${type} Option:`, options)
      }) || [],
    )
    console.log('allModelOptionLoadDone:', modelOptions.value)
  }
  catch (error) {
    console.error('LoadModelOptionfailed:', error)
  }
}

// Based onVoice
function filterVoicesByLanguage() {
  if (!voiceDetails.value || Object.keys(voiceDetails.value).length === 0) {
    voiceOptions.value = []
    return
  }

  const allVoices = Object.values(voiceDetails.value) as any[]
 // Based onSelected of Voice
  const filteredVoices = allVoices.filter((voice) => {
    if (!voice.languages) {
      return false
    }
    const languagesArray = voice.languages.split(/[、；;,，]/).map(lang => lang.trim()).filter(lang => lang)
    return languagesArray.includes(formData.value.language)
  })

  voiceOptions.value = filteredVoices.map(voice => ({
    value: voice.id,
    name: voice.name,
    voiceDemo: voice.voiceDemo,
    voice_demo: voice.voice_demo,
    isClone: Boolean(voice.isClone),
    train_status: voice.trainStatus,
  }))
 // CheckcurrentSelected of VoiceWhether tocurrent，IfthenSelect
  const currentVoiceSupportsLanguage = formData.value.ttsVoiceId
    && filteredVoices.some(voice => voice.id === formData.value.ttsVoiceId)

  if (!currentVoiceSupportsLanguage) {
    formData.value.ttsVoiceId = filteredVoices.length > 0 ? filteredVoices[0].id : ''
    displayNames.value.voiceprint = filteredVoices.length > 0 ? filteredVoices[0].name : ''
  }
  else {
    displayNames.value.voiceprint = filteredVoices.find(item => item.id === formData.value.ttsVoiceId)?.name
  }
 // tottsSettings（Ifvalueisnull，Use0isShowDefaultvalue，Modifyformin of value）
  speedPitchStore.updateSpeedPitch({
    ttsVolume: formData.value.ttsVolume !== null && formData.value.ttsVolume !== undefined ? formData.value.ttsVolume : 0,
    ttsRate: formData.value.ttsRate !== null && formData.value.ttsRate !== undefined ? formData.value.ttsRate : 0,
    ttsPitch: formData.value.ttsPitch !== null && formData.value.ttsPitch !== undefined ? formData.value.ttsPitch : 0,
  })
}

// Based onModelLoad
async function fetchAllLanguag(ttsModelId: string) {
  try {
    const res = await getAllLanguage(ttsModelId)
 // Save of VoiceInfo
    voiceDetails.value = res.reduce((acc, voice) => {
      acc[voice.id] = voice
      return acc
    }, {})
 // allOptionand
    const allLanguages = new Set()
    res.forEach((voice) => {
      if (voice.languages) {
        const languagesArray = voice.languages.split(/[、；;,，]/).map(lang => lang.trim()).filter(lang => lang)
        languagesArray.forEach(lang => allLanguages.add(lang))
      }
    })
    languageOptions.value = Array.from(allLanguages).map(lang => ({
      value: lang,
      name: lang,
    }))
 // UsebackendBack of UserSelect of ，IfhasthenUseOption
    if (formData.value.ttsLanguage && languageOptions.value.some(option => option.value === formData.value.ttsLanguage)) {
      formData.value.language = formData.value.ttsLanguage
      displayNames.value.language = formData.value.ttsLanguage
    }
    else if (languageOptions.value.length > 0) {
      formData.value.language = languageOptions.value[0].value
      displayNames.value.language = languageOptions.value[0].value
    }
 // Based onSelected of Voice
    filterVoicesByLanguage()
  }
  catch {
    languageOptions.value = []
  }
}

// LoadTTSVoiceOption
// async function loadVoiceOptions(ttsModelId?: string) {
//   if (!ttsModelId)
//     return

//   try {
// console.log(`LoadVoiceOption: ${ttsModelId}`)
//     const voices = await getTTSVoices(ttsModelId)
//     voiceOptions.value = voices
//     console.log('VoiceOption:', voices)
//   }
//   catch (error) {
// console.error('LoadVoiceOptionfailed:', error)
//     voiceOptions.value = []
//   }
// }

// SelectRoleTemplate
function selectRoleTemplate(templateId: string) {
  if (selectedTemplateId.value === templateId) {
    selectedTemplateId.value = ''
    return
  }

  selectedTemplateId.value = templateId
  const template = roleTemplates.value.find(t => t.id === templateId)
  if (template) {
    formData.value = {
      ...formData.value,
      systemPrompt: template.systemPrompt || formData.value.systemPrompt,
      vadModelId: template.vadModelId || formData.value.vadModelId,
      asrModelId: template.asrModelId || formData.value.asrModelId,
      llmModelId: template.llmModelId || formData.value.llmModelId,
      slmModelId: template.llmModelId || formData.value.slmModelId,
      vllmModelId: template.vllmModelId || formData.value.vllmModelId,
      intentModelId: template.intentModelId || formData.value.intentModelId,
      memModelId: template.memModelId || formData.value.memModelId,
      ttsModelId: template.ttsModelId || formData.value.ttsModelId,
      ttsVoiceId: template.ttsVoiceId || formData.value.ttsVoiceId,
      agentName: template.agentName || formData.value.agentName,
      chatHistoryConf: template.chatHistoryConf || formData.value.chatHistoryConf,
      summaryMemory: template.summaryMemory || formData.value.summaryMemory,
      langCode: template.langCode || formData.value.langCode,
    }
    fetchAllLanguag(template.ttsModelId || formData.value.ttsModelId)
    updateDisplayNames()
  }
}

// OpenSelect
function openPicker(type: string) {
  pickerShow.value[type] = true
}

// SelectConfirm
async function onPickerConfirm(type: string, value: any, name: string) {
  console.log('SelectConfirm:', type, value, name)
 // SaveShowName
  displayNames.value[type] = name

  switch (type) {
    case 'vad':
      formData.value.vadModelId = value
      break
    case 'asr':
      formData.value.asrModelId = value
      break
    case 'llm':
      formData.value.llmModelId = value
      break
    case 'slm':
      formData.value.slmModelId = value
      break
    case 'vllm':
      formData.value.vllmModelId = value
      break
    case 'intent':
      formData.value.intentModelId = value
      displayNames.value.intent = name // Ensure display name is updated correctly
      break
    case 'memory':
      formData.value.memModelId = value
      formData.value.chatHistoryConf = value === 'Memory_nomem' ? 0 : 2
      displayNames.value.memory = name // Ensure display name is updated correctly
      displayNames.value.report = reportOptions[1].name
      isVisibleReport.value = value !== 'Memory_nomem'
      if (value === 'Memory_nomem' || value === 'Memory_mem_report_only') {
        tempSummaryMemory.value = formData.value.summaryMemory
        formData.value.summaryMemory = ''
      }
      else if (tempSummaryMemory.value !== '' && formData.value.summaryMemory === '') {
        formData.value.summaryMemory = tempSummaryMemory.value
        tempSummaryMemory.value = ''
      }
      break
    case 'tts':
      formData.value.ttsModelId = value
      await fetchAllLanguag(value)
      break
    case 'language':
      formData.value.language = value
      filterVoicesByLanguage()
      break
    case 'voiceprint':
      formData.value.ttsVoiceId = value
      displayNames.value.voiceprint = name // Ensure display name is updated correctly
      break
    case 'report':
      formData.value.chatHistoryConf = value
      break
  }

  pickerShow.value[type] = false
}

// SelectCancel
function onPickerCancel(type: string) {
  pickerShow.value[type] = false
 // ClosewhenStopPlay
  if (type === 'voiceprint') {
    stopAudio()
  }
}

// PlayAudio
function playAudio(voiceDemo: string, voiceId: string, event: Event) {
  event.stopPropagation() // Stop event bubbling to prevent the dropdown from closing

  if (!voiceDemo) {
    return
  }
 // IfatPlayAudio，thenStop
  if (playingVoiceId.value === voiceId) {
    stopAudio()
    return
  }
 // Stop of Audio
  stopAudio()
 // Createnew of Audioinstance
  audioRef.value = uni.createInnerAudioContext()
  audioRef.value.src = voiceDemo
  playingVoiceId.value = voiceId
 // Listen to playbackEnd
  audioRef.value.onEnded(() => {
    playingVoiceId.value = ''
  })
 // Listen to playbackError
  audioRef.value.onError(() => {
    toast.error('Audio playback failed')
    playingVoiceId.value = ''
  })
 // PlayAudio
  audioRef.value.play()
}

// StopAudio
function stopAudio() {
  if (audioRef.value) {
    audioRef.value.stop()
    audioRef.value.destroy()
    audioRef.value = null
  }
  playingVoiceId.value = ''
}

// GetModelShowName
function getModelDisplayName(modelType: string, modelId: string) {
  if (!modelId)
    return 'Please select'
 // directlyfromAPIConfigurationDatain of ID
  const options = modelOptions.value[modelType]

  if (!options || options.length === 0) {
    return modelId
  }

  const option = options.find(opt => opt.id === modelId)
  if (option) {
    return option.modelName
  }
  return modelId
}

// SaveAgent
async function saveAgent() {
  if (!formData.value.agentName?.trim()) {
    toast.warning(t('agent.pleaseInputAgentName'))
    return
  }

  if (!formData.value.systemPrompt?.trim()) {
    toast.warning(t('agent.pleaseInputRoleDescription'))
    return
  }

  try {
    await handleUpdateAgentTags()
  }
  catch (err) {
    toast.error(err)
    return
  }

  try {
    saving.value = true
 // buildSaveData，includesConfigurationandSettings
    const saveData = {
      ...formData.value,
      ...speedPitchStore.speedPitch,
      ttsLanguage: formData.value.language,
      contextProviders: providerStore.providers,
    }
    await updateAgent(agentId.value, saveData)
    loadAgentDetail()

    toast.success(t('agent.saveSuccess'))
  }
  catch (error) {
    console.error('Failed to save:', error)
    toast.error(t('agent.saveFail'))
  }
  finally {
    saving.value = false
  }
}

function loadPluginFunctions() {
  getPluginFunctions().then((res) => {
    const processedFunctions = res?.map((item) => {
      const meta = JSON.parse(item.fields || '[]')
      const params = meta.reduce((m: any, f: any) => {
        m[f.key] = f.default
        return m
      }, {})
      return { ...item, fieldsMeta: meta, params }
    }) || []

    allFunctions.value = processedFunctions
 // whenUpdatetostore
    pluginStore.setAllFunctions(processedFunctions)
  })
}

function handleTools() {
  console.log('currentpluginConfiguration:', formData.value.functions)
 // EnsurestoreinhasnewData
  pluginStore.setCurrentAgentId(agentId.value)
  pluginStore.setCurrentFunctions(formData.value.functions || [])
  pluginStore.setAllFunctions(allFunctions.value)

  uni.navigateTo({
    url: '/pages/agent/tools',
  })
}

// GetAgentTag
async function loadAgentTags() {
  try {
    const res = await getAgentTags(agentId.value)
    dynamicTags.value = res || []
  }
  catch (error) {}
}

// UpdateAgentTag
async function handleUpdateAgentTags() {
  const tagNames = dynamicTags.value.map(tag => tag.tagName)
  await updateAgentTags(agentId.value, { tagNames })
}

// listen tostorein of pluginConfigurationchange
watch(() => pluginStore.currentFunctions, (newFunctions) => {
  formData.value.functions = newFunctions
}, { deep: true })

onMounted(async () => {
  loadAgentTags()
 // firstLoadModelOptionandRoleTemplate
  await Promise.all([
    loadRoleTemplates(),
    loadModelOptions(),
    loadPluginFunctions(),
  ])
 // afterLoadAgentdetails，thisShowName
  if (agentId.value) {
    await loadAgentDetail()
  }
})
</script>

<template>
  <view class="bg-[#f5f7fb] px-[20rpx]">
    <!-- InfoTitle <view class="pb-[20rpx] first:pt-[20rpx]"> <text class="text-[32rpx] text-[#232338] font-bold"> {{ t('agent.basicInfo') }} </text> </view -->

    <!-- Infocard -->
    <view class="mb-[24rpx] border border-[#eeeeee] rounded-[20rpx] bg-[#fbfbfb] p-[24rpx]" style="box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);">
      <view class="mb-[24rpx] last:mb-0">
        <text class="mb-[12rpx] block text-[28rpx] text-[#232338] font-medium">
          {{ t('agent.agentName') }}
        </text>
        <input
          v-model="formData.agentName"
          class="box-border h-[80rpx] w-full border border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[16rpx_20rpx] text-[28rpx] text-[#232338] leading-[1.4] outline-none focus:border-[#336cff] focus:bg-white placeholder:text-[#9d9ea3]"
          type="text"
          :placeholder="t('agent.inputAgentName')"
        >
      </view>

      <view class="mb-[24rpx] last:mb-0">
        <text class="mb-[12rpx] block text-[28rpx] text-[#232338] font-medium">
          {{ t('agent.agentTag') }}
        </text>
        <input
          v-if="inputVisible"
          v-model="inputValue"
          class="mb-[10rpx] box-border h-[80rpx] w-full border border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[16rpx_20rpx] text-[28rpx] text-[#232338] leading-[1.4] outline-none focus:border-[#336cff] focus:bg-white placeholder:text-[#9d9ea3]"
          type="text"
          :maxlength="20"
          :placeholder="t('agent.inputAgentTag')"
          @keyup.enter="handleInputConfirm"
          @blur="handleInputConfirm"
        >
        <view class="flex flex-wrap gap-[10rpx_10rpx]">
          <wd-tag v-for="tag in dynamicTags" :key="tag.id" class="items-center border !flex !border-[rgba(51,108,255,0.2)] !bg-[rgba(51,108,255,0.1)] !text-[#336cff]" round closable @close="handleCloseTag(tag.id)">
            {{ tag.tagName }}
          </wd-tag>
          <wd-button v-if="!inputVisible" class="!bg-[rgba(51,108,255,0.1)] !text-[#336cff]" size="small" icon="add" @click="showInput">
            {{ t('agent.addAgentTag') }}
          </wd-button>
        </view>
      </view>

      <view class="mb-[24rpx] last:mb-0">
        <text class="mb-[12rpx] block text-[28rpx] text-[#232338] font-medium">
          {{ t('agent.roleMode') }}
        </text>
        <view class="mt-0 flex flex-wrap gap-[12rpx]">
          <view
            v-for="template in roleTemplates"
            :key="template.id"
            class="cursor-pointer rounded-[20rpx] px-[24rpx] py-[12rpx] text-[24rpx] transition-all duration-300"
            :class="selectedTemplateId === template.id
              ? 'bg-[#336cff] text-white border border-[#336cff]'
              : 'bg-[rgba(51,108,255,0.1)] text-[#336cff] border border-[rgba(51,108,255,0.2)]'"
            @click="selectRoleTemplate(template.id)"
          >
            {{ template.agentName }}
          </view>
        </view>
      </view>

      <view class="mb-[24rpx] last:mb-0">
        <text class="mb-[12rpx] block text-[28rpx] text-[#232338] font-medium">
          {{ t('agent.contextProvider') }}
        </text>
        <view class="mt-0 flex flex-wrap items-center gap-[12rpx]">
          <text class="text-[26rpx] text-[#65686f]">
            {{ t('agent.contextProviderSuccess', { count: providerStore.providers.length }) }}
          </text>
          <a class="text-[26rpx] text-[#5778ff] no-underline" href="https://github.com/xinnan-tech/xiaozhi-esp32-server/blob/main/docs/context-provider-integration.md" target="_blank">
            {{ t('agent.contextProviderDocLink') }}
          </a>
          <wd-button class="!bg-[rgba(51,108,255,0.1)] !text-[#336cff]" size="small" @click="openContextProviderDialog">
            {{ t('agent.editContextProvider') }}
          </wd-button>
        </view>
      </view>

      <view class="mb-[24rpx] last:mb-0">
        <text class="mb-[12rpx] block text-[28rpx] text-[#232338] font-medium">
          {{ t('agent.roleDescription') }}
        </text>
        <textarea
          v-model="formData.systemPrompt"
          :maxlength="2000"
          :placeholder="t('agent.inputRoleDescription')"
          class="box-border h-[500rpx] w-full resize-none break-words break-all border border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[20rpx] text-[26rpx] text-[#232338] leading-[1.6] outline-none focus:border-[#336cff] focus:bg-white placeholder:text-[#9d9ea3]"
        />
        <view class="mt-[8rpx] text-right text-[22rpx] text-[#9d9ea3]">
          {{ (formData.systemPrompt || '').length }}/2000
        </view>
      </view>
    </view>

    <!-- Model configurationTitle -->
    <view class="pb-[20rpx]">
      <text class="text-[32rpx] text-[#232338] font-bold">
        {{ t('agent.modelConfig') }}
      </text>
    </view>

    <!-- Model configurationcard -->
    <view class="mb-[24rpx] border border-[#eeeeee] rounded-[20rpx] bg-[#fbfbfb] p-[24rpx]" style="box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);">
      <view class="flex flex-col gap-[16rpx]">
        <view class="flex cursor-pointer items-center justify-between border border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[20rpx] transition-all duration-300 active:bg-[#eef3ff]" @click="openPicker('vad')">
          <text class="text-[28rpx] text-[#232338] font-medium">
            {{ t('agent.vad') }}
          </text>
          <text class="mx-[16rpx] flex-1 text-right text-[26rpx] text-[#65686f]">
            {{ displayNames.vad }}
          </text>
          <wd-icon name="arrow-right" custom-class="text-[20rpx] text-[#9d9ea3]" />
        </view>

        <view class="flex cursor-pointer items-center justify-between border border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[20rpx] transition-all duration-300 active:bg-[#eef3ff]" @click="openPicker('asr')">
          <text class="text-[28rpx] text-[#232338] font-medium">
            {{ t('agent.asr') }}
          </text>
          <text class="mx-[16rpx] flex-1 text-right text-[26rpx] text-[#65686f]">
            {{ displayNames.asr }}
          </text>
          <wd-icon name="arrow-right" custom-class="text-[20rpx] text-[#9d9ea3]" />
        </view>

        <view class="flex cursor-pointer items-center justify-between border border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[20rpx] transition-all duration-300 active:bg-[#eef3ff]" @click="openPicker('llm')">
          <text class="text-[28rpx] text-[#232338] font-medium">
            {{ t('agent.llm') }}
          </text>
          <text class="mx-[16rpx] flex-1 text-right text-[26rpx] text-[#65686f]">
            {{ displayNames.llm }}
          </text>
          <wd-icon name="arrow-right" custom-class="text-[20rpx] text-[#9d9ea3]" />
        </view>

        <view class="flex cursor-pointer items-center justify-between border border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[20rpx] transition-all duration-300 active:bg-[#eef3ff]" @click="openPicker('slm')">
          <text class="text-[28rpx] text-[#232338] font-medium">
            {{ t('agent.slm') }}
          </text>
          <text class="mx-[16rpx] flex-1 text-right text-[26rpx] text-[#65686f]">
            {{ displayNames.slm }}
          </text>
          <wd-icon name="arrow-right" custom-class="text-[20rpx] text-[#9d9ea3]" />
        </view>

        <view class="flex cursor-pointer items-center justify-between border border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[20rpx] transition-all duration-300 active:bg-[#eef3ff]" @click="openPicker('vllm')">
          <text class="text-[28rpx] text-[#232338] font-medium">
            {{ t('agent.vllm') }}
          </text>
          <text class="mx-[16rpx] flex-1 text-right text-[26rpx] text-[#65686f]">
            {{ displayNames.vllm }}
          </text>
          <wd-icon name="arrow-right" custom-class="text-[20rpx] text-[#9d9ea3]" />
        </view>

        <view class="flex cursor-pointer items-center justify-between border border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[20rpx] transition-all duration-300 active:bg-[#eef3ff]" @click="openPicker('intent')">
          <text class="text-[28rpx] text-[#232338] font-medium">
            {{ t('agent.intent') }}
          </text>
          <text class="mx-[16rpx] flex-1 text-right text-[26rpx] text-[#65686f]">
            {{ displayNames.intent }}
          </text>
          <wd-icon name="arrow-right" custom-class="text-[20rpx] text-[#9d9ea3]" />
        </view>

        <view class="flex cursor-pointer items-center justify-between border border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[20rpx] transition-all duration-300 active:bg-[#eef3ff]" @click="openPicker('memory')">
          <text class="text-[28rpx] text-[#232338] font-medium">
            {{ t('agent.memory') }}
          </text>
          <text class="mx-[16rpx] flex-1 text-right text-[26rpx] text-[#65686f]">
            {{ displayNames.memory }}
          </text>
          <wd-icon name="arrow-right" custom-class="text-[20rpx] text-[#9d9ea3]" />
        </view>

        <view v-show="isVisibleReport" class="flex cursor-pointer items-center justify-between border border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[20rpx] transition-all duration-300 active:bg-[#eef3ff]" @click="openPicker('report')">
          <text class="text-[28rpx] text-[#232338] font-medium">
            {{ t('agent.reportMode') }}
          </text>
          <text class="mx-[16rpx] flex-1 text-right text-[26rpx] text-[#65686f]">
            {{ displayNames.report }}
          </text>
          <wd-icon name="arrow-right" custom-class="text-[20rpx] text-[#9d9ea3]" />
        </view>
      </view>
    </view>

    <!-- SettingsTitle -->
    <view class="pb-[20rpx]">
      <text class="text-[32rpx] text-[#232338] font-bold">
        {{ t('agent.voiceSettings') }}
      </text>
    </view>

    <!-- Settingscard -->
    <view class="mb-[24rpx] border border-[#eeeeee] rounded-[20rpx] bg-[#fbfbfb] p-[24rpx]" style="box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);">
      <view class="flex flex-col gap-[16rpx]">
        <view class="flex cursor-pointer items-center justify-between border border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[20rpx] transition-all duration-300 active:bg-[#eef3ff]" @click="openPicker('tts')">
          <text class="text-[28rpx] text-[#232338] font-medium">
            {{ t('agent.tts') }}
          </text>
          <text class="mx-[16rpx] flex-1 text-right text-[26rpx] text-[#65686f]">
            {{ displayNames.tts }}
          </text>
          <wd-icon name="arrow-right" custom-class="text-[20rpx] text-[#9d9ea3]" />
        </view>

        <view class="flex cursor-pointer items-center justify-between border border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[20rpx] transition-all duration-300 active:bg-[#eef3ff]" @click="openPicker('language')">
          <text class="text-[28rpx] text-[#232338] font-medium">
            {{ t('agent.language') }}
          </text>
          <text class="mx-[16rpx] flex-1 text-right text-[26rpx] text-[#65686f]">
            {{ displayNames.language }}
          </text>
          <wd-icon name="arrow-right" custom-class="text-[20rpx] text-[#9d9ea3]" />
        </view>

        <view class="flex cursor-pointer items-center justify-between border border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[20rpx] transition-all duration-300 active:bg-[#eef3ff]" @click="openPicker('voiceprint')">
          <text class="text-[28rpx] text-[#232338] font-medium">
            {{ t('agent.voiceprint') }}
          </text>
          <text class="mx-[16rpx] flex-1 text-right text-[26rpx] text-[#65686f]">
            {{ displayNames.voiceprint }}
          </text>
          <wd-icon name="arrow-right" custom-class="text-[20rpx] text-[#9d9ea3]" />
        </view>

        <view class="flex items-center justify-between border border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[20rpx]">
          <view class="text-[28rpx] text-[#232338] font-medium">
            {{ t('agent.languageConfig') }}
          </view>
          <view class="cursor-pointer rounded-[20rpx] bg-[rgba(51,108,255,0.1)] px-[24rpx] py-[12rpx] text-[24rpx] text-[#336cff] transition-all duration-300 active:bg-[#336cff] active:text-white" @click="handleRegulate">
            <text>{{ t('agent.editFunctions') }}</text>
          </view>
        </view>

        <view class="flex items-center justify-between border border-[#eeeeee] rounded-[12rpx] bg-[#f5f7fb] p-[20rpx]">
          <view class="text-[28rpx] text-[#232338] font-medium">
            {{ t('agent.plugins') }}
          </view>
          <view class="cursor-pointer rounded-[20rpx] bg-[rgba(51,108,255,0.1)] px-[24rpx] py-[12rpx] text-[24rpx] text-[#336cff] transition-all duration-300 active:bg-[#336cff] active:text-white" @click="handleTools">
            <text>{{ t('agent.editFunctions') }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- HistoryTitle -->
    <view class="pb-[20rpx]">
      <text class="text-[32rpx] text-[#232338] font-bold">
        {{ t('agent.historyMemory') }}
      </text>
    </view>

    <!-- Historycard -->
    <view class="mb-[24rpx] border border-[#eeeeee] rounded-[20rpx] bg-[#fbfbfb] p-[24rpx]" style="box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);">
      <view class="mb-[24rpx] last:mb-0">
        <textarea
          v-model="formData.summaryMemory"
          :placeholder="t('agent.memoryContent')"
          :disabled="isMemoryDisabled"
          :style="isMemoryDisabled ? 'background: #f0f0f0' : ''"
          class="box-border h-[500rpx] w-full resize-none break-words break-all border border-[#eeeeee] rounded-[12rpx] p-[20rpx] text-[26rpx] leading-[1.6] opacity-80 outline-none"
        />
      </view>
    </view>

    <!-- SaveButton -->
    <view class="mt-[40rpx] p-0">
      <wd-button
        type="primary"
        :loading="saving"
        :disabled="saving"
        custom-class="w-full h-[80rpx] rounded-[16rpx] text-[30rpx] font-semibold bg-[#336cff] active:bg-[#2d5bd1]"
        @click="saveAgent"
      >
        {{ saving ? t('agent.saving') : t('agent.save') }}
      </wd-button>
    </view>
    <!-- ModelSelect -->
    <wd-action-sheet
      v-model="pickerShow.vad"
      :actions="modelOptions.VAD && modelOptions.VAD.map(item => ({ name: item.modelName, value: item.id }))"
      @close="onPickerCancel('vad')"
      @select="({ item }) => onPickerConfirm('vad', item.value, item.name)"
    />

    <wd-action-sheet
      v-model="pickerShow.asr"
      :actions="modelOptions.ASR && modelOptions.ASR.map(item => ({ name: item.modelName, value: item.id }))"
      @close="onPickerCancel('asr')"
      @select="({ item }) => onPickerConfirm('asr', item.value, item.name)"
    />

    <wd-action-sheet
      v-model="pickerShow.llm"
      :actions="modelOptions.LLM && modelOptions.LLM.map(item => ({ name: item.modelName, value: item.id }))"
      @close="onPickerCancel('llm')"
      @select="({ item }) => onPickerConfirm('llm', item.value, item.name)"
    />

    <wd-action-sheet
      v-model="pickerShow.slm"
      :actions="modelOptions.LLM && modelOptions.LLM.map(item => ({ name: item.modelName, value: item.id }))"
      @close="onPickerCancel('slm')"
      @select="({ item }) => onPickerConfirm('slm', item.value, item.name)"
    />

    <wd-action-sheet
      v-model="pickerShow.vllm"
      :actions="modelOptions.VLLM && modelOptions.VLLM.map(item => ({ name: item.modelName, value: item.id }))"
      @close="onPickerCancel('vllm')"
      @select="({ item }) => onPickerConfirm('vllm', item.value, item.name)"
    />

    <wd-action-sheet
      v-model="pickerShow.intent"
      :actions="modelOptions.Intent && modelOptions.Intent.map(item => ({ name: item.modelName, value: item.id }))"
      @close="onPickerCancel('intent')"
      @select="({ item }) => onPickerConfirm('intent', item.value, item.name)"
    />

    <wd-action-sheet
      v-model="pickerShow.memory"
      :actions="modelOptions.Memory && modelOptions.Memory.map(item => ({ name: item.modelName, value: item.id }))"
      @close="onPickerCancel('memory')"
      @select="({ item }) => onPickerConfirm('memory', item.value, item.name)"
    />

    <wd-action-sheet
      v-model="pickerShow.tts"
      :actions="modelOptions.TTS && modelOptions.TTS.map(item => ({ name: item.modelName, value: item.id }))"
      class="custom-sheet-tts"
      @close="onPickerCancel('tts')"
      @select="({ item }) => onPickerConfirm('tts', item.value, item.name)"
    />

    <!-- customSelect -->
    <wd-popup v-model="pickerShow.voiceprint" class="custom-popup" position="bottom" @close="onPickerCancel('voiceprint')">
      <view class="overflow-hidden rounded-[20rpx] bg-white pb-[20rpx] pt-[20rpx]">
        <view class="max-h-[600rpx] overflow-y-auto">
          <view
            v-for="voice in voiceOptions"
            :key="voice.value"
            class="flex items-center justify-between border-b border-[#f5f5f5] p-[32rpx] transition-all active:bg-[#f5f7fb]"
            @click="onPickerConfirm('voiceprint', voice.value, voice.name)"
          >
            <text :class="`flex-1 text-[28rpx] text-[#232338] ${(voice.voiceDemo || voice.voice_demo) ? '' : 'text-center'}`">
              {{ voice.name }}
            </text>
            <view v-if="voice.voiceDemo || voice.voice_demo" class="ml-[20rpx]" @click.stop="playAudio(voice.voiceDemo || voice.voice_demo, voice.value, $event)">
              <wd-icon
                :name="playingVoiceId === voice.value ? 'pause-circle' : 'play-circle'"
                size="24px"
                :custom-class="playingVoiceId === voice.value ? 'text-[#336cff]' : 'text-[#9d9ea3]'"
              />
            </view>
          </view>
        </view>
      </view>
    </wd-popup>
    <wd-action-sheet
      v-model="pickerShow.language"
      :actions="languageOptions"
      @close="onPickerCancel('language')"
      @select="({ item }) => onPickerConfirm('language', item.value, item.name)"
    />
    <wd-action-sheet
      v-model="pickerShow.report"
      :actions="reportOptions"
      @close="onPickerCancel('report')"
      @select="({ item }) => onPickerConfirm('report', item.value, item.name)"
    />
  </view>
</template>

<style lang="scss" scoped>
::v-deep .wd-tag__close {
  color: #336cff !important;
}
::v-deep .custom-popup {
  .wd-popup {
    padding: 20rpx !important;
    background: transparent !important;
  }
}
::v-deep .custom-sheet-tts {
  .wd-action-sheet {
    padding: 8px 0 !important;
    overflow: hidden;
  }
  .wd-action-sheet__actions {
    padding: 0 !important;
  }
}
</style>
