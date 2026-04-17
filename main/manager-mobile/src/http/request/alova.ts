import type { uniappRequestAdapter } from '@alova/adapter-uniapp'
import type { IResponse } from './types'
import type { Language } from '@/store/lang'
import AdapterUniapp from '@alova/adapter-uniapp'
import { createAlova } from 'alova'
import { createServerTokenAuthentication } from 'alova/client'
import VueHook from 'alova/vue'
import { getEnvBaseUrl } from '@/utils'
import { toast } from '@/utils/toast'
import { ContentTypeEnum, ResultEnum, ShowMessage } from './enum'

// Used to set the Accept-language header
const langMap: Record<Language, string> = {
  zh_CN: 'zh-CN',
  en: 'en-US',
  zh_TW: 'zh-TW',
  de: 'de',
  vi: 'vi',
  pt_BR: 'pt-BR',
}

/** Create request instance */
const { onAuthRequired, onResponseRefreshToken } = createServerTokenAuthentication<
  typeof VueHook,
  typeof uniappRequestAdapter
>({
  refreshTokenOnError: {
    isExpired: (error) => {
      return error.response?.status === ResultEnum.Unauthorized
    },
    handler: async () => {
      try {
        // await authLogin();
      }
      catch (error) {
        // Switch to the login page
        await uni.reLaunch({ url: '/pages/login/index' })
        throw error
      }
    },
  },
})

/** alova request instance */
const alovaInstance = createAlova({
  baseURL: getEnvBaseUrl(),
  ...AdapterUniapp(),
  timeout: 5000,
  statesHook: VueHook,

  beforeRequest: onAuthRequired((method) => {
    // On H5, get the latest baseURL to honor the user-configured server address
    const currentBaseUrl = getEnvBaseUrl()
    if (currentBaseUrl !== method.baseURL) {
      method.baseURL = currentBaseUrl
    }
    // Check for mixed content error (HTTPS page requesting HTTP endpoint)
    const currentProtocol = typeof window !== 'undefined' && window.location.protocol
    const requestProtocol = method.baseURL?.split(':')[0]
    const currentLang = langMap[uni.getStorageSync('app_language') as Language || 'en']
    if (currentProtocol === 'https:' && requestProtocol === 'http') {
      const errorMessage = 'Cannot use HTTP protocol address; please check the API URL'
      throw new Error(errorMessage)
    }

    // Set default Content-Type
    method.config.headers = {
      'Content-Type': ContentTypeEnum.JSON,
      'Accept': 'application/json, text/plain, */*',
      'Accept-language': currentLang,
      ...method.config.headers,
    }

    const { config } = method
    const ignoreAuth = config.meta?.ignoreAuth
    console.log('ignoreAuth===>', ignoreAuth)
    // Process authentication info
    if (!ignoreAuth) {
      const authInfo = JSON.parse(uni.getStorageSync('token') || '{}')
      if (!authInfo.token) {
        // Redirect to the login page
        uni.reLaunch({ url: '/pages/login/index' })
        throw new Error('[Request error]: not logged in')
      }
      // Add Authorization header
      method.config.headers.Authorization = `Bearer ${authInfo.token}`
    }
    // Process custom domain
    if (config.meta?.domain) {
      method.baseURL = config.meta.domain
      console.log('current', method.baseURL)
    }
  }),

  responded: onResponseRefreshToken((response, method) => {
    const { config } = method
    const { requestType } = config
    const {
      statusCode,
      data: rawData,
      errMsg,
    } = response as UniNamespace.RequestSuccessCallbackResult

    console.log(response)
    // Handle requestType (upload / download)
    if (requestType === 'upload' || requestType === 'download') {
      return response
    }
    // Handle HTTP status-code errors
    if (statusCode !== 200) {
      const errorMessage = ShowMessage(statusCode) || `HTTP request error [${statusCode}]`
      console.error('errorMessage===>', errorMessage)
      toast.error(errorMessage)
      throw new Error(`${errorMessage}: ${errMsg}`)
    }
    // Handle logical errors
    const { code, msg, data } = rawData as IResponse
    if (code !== ResultEnum.Success) {
      // Check whether the token is invalid
      if (code === ResultEnum.Unauthorized) {
        // Clear the token and redirect to the login page
        uni.removeStorageSync('token')
        uni.reLaunch({ url: '/pages/login/index' })
        throw new Error(`Request error [${code}]: ${msg}`)
      }

      if (config.meta?.isExposeError) {
        return Promise.reject(msg)
      }

      if (config.meta?.toast !== false) {
        toast.warning(msg)
      }
      throw new Error(`Request error [${code}]: ${msg}`)
    }
    // On successful response, return the data
    return data
  }),
})

export const http = alovaInstance
