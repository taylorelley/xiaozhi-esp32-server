import { ref } from 'vue'
import { useLangStore } from '@/store/lang'
import type { Language } from '@/store/lang'

// Import the translation files for each language.
import zh_CN from './zh_CN'
import en from './en'
import zh_TW from './zh_TW'
import de from './de'
import vi from './vi'
import pt_BR from './pt_BR'

// Language pack mapping
const messages = {
  zh_CN: zh_CN,
  en,
  zh_TW: zh_TW,
  de,
  vi,
  pt_BR: pt_BR,
}

// Currently active language
const currentLang = ref<Language>('en')

// Initialize the language
export function initI18n() {
  const langStore = useLangStore()
  currentLang.value = langStore.currentLang
}

// Switch language
export function changeLanguage(lang: Language) {
  currentLang.value = lang
  const langStore = useLangStore()
  langStore.changeLang(lang)
}

// Get translated text
export function t(key: string, params?: Record<string, string | number>): string {
  const langMessages = messages[currentLang.value]

  // Look up flat key directly
  if (langMessages && typeof langMessages === 'object' && key in langMessages) {
    let value = langMessages[key]
    if (typeof value === 'string') {
      // Handle parameter substitution
      if (params) {
        let result = value
        Object.entries(params).forEach(([paramKey, paramValue]) => {
          const regex = new RegExp(`\{${paramKey}\}`, 'g')
          result = result.replace(regex, String(paramValue))
        })
        return result
      }
      return value
    }
    return key
  }

  return key // If no translation is found, return the key itself.
}

// Get the current language
export function getCurrentLanguage(): Language {
  return currentLang.value
}

// Get the list of supported languages
export function getSupportedLanguages(): { code: Language, name: string }[] {
  return [
    { code: 'zh_CN', name: '简体中文' },
    { code: 'en', name: 'English' },
    { code: 'zh_TW', name: '繁體中文' },
    { code: 'de', name: 'Deutsch' },
    { code: 'vi', name: 'Tiếng Việt' },
    { code: 'pt_BR', name: 'Português (Brasil)' },
  ]
}