import smCrypto from 'sm-crypto'
import { pages, subPackages } from '@/pages.json'

import { isMpWeixin } from './platform'

/** * whenserverAddressStorage */
export const SERVER_BASE_URL_OVERRIDE_KEY = 'server_base_url_override'

/** * Settings/Clear/Get when of serverAddress */
export function setServerBaseUrlOverride(url: string) {
  uni.setStorageSync(SERVER_BASE_URL_OVERRIDE_KEY, url)
}

export function clearServerBaseUrlOverride() {
  uni.removeStorageSync(SERVER_BASE_URL_OVERRIDE_KEY)
}

export function getServerBaseUrlOverride(): string | null {
  const value = uni.getStorageSync(SERVER_BASE_URL_OVERRIDE_KEY)
  return value || null
}

export function getLastPage() {
 // getCurrentPages() has1，Check
  // const lastPage = getCurrentPages().at(-1)
 // thatatin，this【 src/interceptions/prototype.ts，】
  const pages = getCurrentPages()
  return pages[pages.length - 1]
}

/** * GetcurrentPage of path Pathand redirectPath Path * path such as '/pages/login/index' * redirectPath such as '/pages/demo/base/route-interceptor' */
export function currRoute() {
  const lastPage = getLastPage()
  const currRoute = (lastPage as any).$page
  // console.log('lastPage.$page:', currRoute)
  // console.log('lastPage.$page.fullpath:', currRoute.fullPath)
  // console.log('lastPage.$page.options:', currRoute.options)
  // console.log('lastPage.options:', (lastPage as any).options)
 // Test，has fullPath ，
  const { fullPath } = currRoute as { fullPath: string }
  // console.log(fullPath)
 // eg: /pages/login/index?redirect=%2Fpages%2Fdemo%2Fbase%2Froute-interceptor ()
  // eg: /pages/login/index?redirect=%2Fpages%2Froute-interceptor%2Findex%3Fname%3Dfeige%26age%3D30(h5)
  return getUrlObj(fullPath)
}

function ensureDecodeURIComponent(url: string) {
  if (url.startsWith('%')) {
    return ensureDecodeURIComponent(decodeURIComponent(url))
  }
  return url
}
/** * Parse url to path and query * such asurl: /pages/login/index?redirect=%2Fpages%2Fdemo%2Fbase%2Froute-interceptor * : {path: /pages/login/index, query: {redirect: /pages/demo/base/route-interceptor}} */
export function getUrlObj(url: string) {
  const [path, queryStr] = url.split('?')
  // console.log(path, queryStr)

  if (!queryStr) {
    return {
      path,
      query: {},
    }
  }
  const query: Record<string, string> = {}
  queryStr.split('&').forEach((item) => {
    const [key, value] = item.split('=')
    // console.log(key, value)
    query[key] = ensureDecodeURIComponent(value) // 这里需要统一 decodeURIComponent 一下，可以兼容h5和微信y
  })
  return { path, query }
}
/** * toall of needsLog in of pages，and of * this， key isCheck，Defaultis needLogin, route-block Use * Ifhas key，thenall of pages，If key, then key Filter */
export function getAllPages(key = 'needLogin') {
 // thisProcess
  const mainPages = pages
    .filter(page => !key || page[key])
    .map(page => ({
      ...page,
      path: `/${page.path}`,
    }))
 // thisProcess
  const subPages: any[] = []
  subPackages.forEach((subPageObj) => {
    // console.log(subPageObj)
    const { root } = subPageObj

    subPageObj.pages
      .filter(page => !key || page[key])
      .forEach((page: { path: string } & Record<string, any>) => {
        subPages.push({
          ...page,
          path: `/${root}/${page.path}`,
        })
      })
  })
  const result = [...mainPages, ...subPages]
  // console.log(`getAllPages by ${key} result: `, result)
  return result
}

/** * toall of needsLog in of pages，and of * to path Array */
export const getNeedLoginPages = (): string[] => getAllPages('needLogin').map(page => page.path)

/** * toall of needsLog in of pages，and of * to path Array */
export const needLoginPages: string[] = getAllPages('needLogin').map(page => page.path)

/** * Based oncurrent，CheckGet of baseUrl */
export function getEnvBaseUrl() {
 // atUserSettings of Address，firstBack
  const override = getServerBaseUrlOverride()
  if (override)
    return override
 // requestAddress（Default env）
  let baseUrl = import.meta.env.VITE_SERVER_BASEURL
 // # hasneedsatBased on develop、trial、release SettingsUploadAddress，referencecodesuch as。
  const VITE_SERVER_BASEURL__WEIXIN_DEVELOP = 'https://ukw0y1.laf.run'
  const VITE_SERVER_BASEURL__WEIXIN_TRIAL = 'https://ukw0y1.laf.run'
  const VITE_SERVER_BASEURL__WEIXIN_RELEASE = 'https://ukw0y1.laf.run'
 // 
  if (isMpWeixin) {
    const {
      miniProgram: { envVersion },
    } = uni.getAccountInfoSync()

    switch (envVersion) {
      case 'develop':
        baseUrl = VITE_SERVER_BASEURL__WEIXIN_DEVELOP || baseUrl
        break
      case 'trial':
        baseUrl = VITE_SERVER_BASEURL__WEIXIN_TRIAL || baseUrl
        break
      case 'release':
        baseUrl = VITE_SERVER_BASEURL__WEIXIN_RELEASE || baseUrl
        break
    }
  }

  return baseUrl
}

/** * Based oncurrent，CheckGet of UPLOAD_BASEURL */
export function getEnvBaseUploadUrl() {
 // requestAddress
  let baseUploadUrl = import.meta.env.VITE_UPLOAD_BASEURL

  const VITE_UPLOAD_BASEURL__WEIXIN_DEVELOP = 'https://ukw0y1.laf.run/upload'
  const VITE_UPLOAD_BASEURL__WEIXIN_TRIAL = 'https://ukw0y1.laf.run/upload'
  const VITE_UPLOAD_BASEURL__WEIXIN_RELEASE = 'https://ukw0y1.laf.run/upload'
 // 
  if (isMpWeixin) {
    const {
      miniProgram: { envVersion },
    } = uni.getAccountInfoSync()

    switch (envVersion) {
      case 'develop':
        baseUploadUrl = VITE_UPLOAD_BASEURL__WEIXIN_DEVELOP || baseUploadUrl
        break
      case 'trial':
        baseUploadUrl = VITE_UPLOAD_BASEURL__WEIXIN_TRIAL || baseUploadUrl
        break
      case 'release':
        baseUploadUrl = VITE_UPLOAD_BASEURL__WEIXIN_RELEASE || baseUploadUrl
        break
    }
  }

  return baseUploadUrl
}

/** * GenerateSM2Key（hex format） * @returns {Object} includesPublic keyandPrivate key of Object */
export function generateSm2KeyPairHex() {
 // Usesm-cryptoGenerateSM2Key
  const sm2 = smCrypto.sm2
  const keypair = sm2.generateKeyPairHex()

  return {
    publicKey: keypair.publicKey,
    privateKey: keypair.privateKey,
    clientPublicKey: keypair.publicKey, // 客户端公钥
    clientPrivateKey: keypair.privateKey, // 客户端私钥
  }
}

/** * SM2Public keyEncrypt * @param {string} publicKey Public key（hex format） * @param {string} plainText * @returns {string} Encryptafter of （hex format） */
export function sm2Encrypt(publicKey: string, plainText: string): string {
  if (!publicKey) {
    throw new Error('公钥不能为null或undefined')
  }

  if (!plainText) {
    throw new Error('明文不能为空')
  }

  const sm2 = smCrypto.sm2
 // SM2Encrypt，Add04Public key
  const encrypted = sm2.doEncrypt(plainText, publicKey, 1)
 // Convert tohex format（backend，Add04）
  const result = `04${encrypted}`

  return result
}

/** * SM2Private keyDecrypt * @param {string} privateKey Private key（hex format） * @param {string} cipherText （hex format） * @returns {string} Decryptafter of */
export function sm2Decrypt(privateKey: string, cipherText: string): string {
  const sm2 = smCrypto.sm2
 // Remove04（backend）
  const dataWithoutPrefix = cipherText.startsWith('04') ? cipherText.substring(2) : cipherText
  // SM2Decrypt
  return sm2.doDecrypt(dataWithoutPrefix, privateKey, 1)
}

type AnyFunction = (...args: any[]) => any

interface DebouncedFunction extends AnyFunction {
  cancel: () => void
}

/** * Function * @param fn of Function * @param delay when（），Default500ms * @param immediate Whether to，Defaultfalse * @returns Processafter of Function */
export function debounce<T extends AnyFunction>(
  fn: T,
  delay = 500,
  immediate = false,
): DebouncedFunction {
  let timer: ReturnType<typeof setTimeout> | null = null

  const debounced = function (this: any, ...args: Parameters<T>) {
    if (timer) {
      clearTimeout(timer)
    }

    if (immediate && !timer) {
      fn.apply(this, args)
    }

    timer = setTimeout(() => {
      if (!immediate) {
        fn.apply(this, args)
      }
      timer = null
    }, delay)
  } as DebouncedFunction

  debounced.cancel = () => {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  return debounced
}

type DeepCloneTarget = string | number | boolean | null | undefined | object

/** * Method * @param target of * @returns after of newObject */
export function deepClone<T extends DeepCloneTarget>(target: T): T {
  if (target === null || typeof target !== 'object') {
    return target
  }

  if (target instanceof Date) {
    return new Date(target.getTime()) as any
  }

  if (Array.isArray(target)) {
    return target.map(item => deepClone(item)) as any
  }

  if (target instanceof Object) {
    const clonedObj = {} as T
    for (const key in target) {
      if (Object.prototype.hasOwnProperty.call(target, key)) {
        (clonedObj as any)[key] = deepClone((target as any)[key])
      }
    }
    return clonedObj
  }

  return target
}
