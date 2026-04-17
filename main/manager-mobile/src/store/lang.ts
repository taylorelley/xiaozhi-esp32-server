import { ref } from 'vue'
import { defineStore } from 'pinia'

// Supported language types
export type Language = 'zh_CN' | 'en' | 'zh_TW' | 'de' | 'vi' | 'pt_BR'

export interface LangStore {
  currentLang: Language
  changeLang: (lang: Language) => void
}

export const useLangStore = defineStore(
  'lang',
  (): LangStore => {
    // Get the language setting from local storage. If missing, use the default value.
    const savedLang = uni.getStorageSync('app_language') as Language | null
    const currentLang = ref<Language>(savedLang || 'en')

    // Switch language
    const changeLang = (lang: Language) => {
      currentLang.value = lang
      // Save the language setting to local storage
      uni.setStorageSync('app_language', lang)
    }

    return {
      currentLang,
      changeLang,
    }
  },
  {
    persist: {
      key: 'lang',
      serializer: {
        serialize: state => JSON.stringify(state.currentLang),
        deserialize: value => ({ currentLang: JSON.parse(value) }),
      },
    },
  },
)