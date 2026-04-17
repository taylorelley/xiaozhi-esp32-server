import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store'
import { needLoginPages as _needLoginPages, getNeedLoginPages } from '@/utils'

const loginRoute = import.meta.env.VITE_LOGIN_URL
const isDev = import.meta.env.DEV
function isLogined() {
  const userStore = useUserStore()
  return !!userStore.userInfo.username
}
// CheckcurrentPageWhether toneedsLog in
export function usePageAuth() {
  onLoad((options) => {
 // GetcurrentPagePath
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const currentPath = `/${currentPage.route}`
 // GetneedsLog in of Pagelist
    let needLoginPages: string[] = []
    if (isDev) {
      needLoginPages = getNeedLoginPages()
    }
    else {
      needLoginPages = _needLoginPages
    }
 // CheckcurrentPageWhether toneedsLog in
    const isNeedLogin = needLoginPages.includes(currentPath)
    if (!isNeedLogin) {
      return
    }

    const hasLogin = isLogined()
    if (hasLogin) {
      return true
    }
 // buildURL
    const queryString = Object.entries(options || {})
      .map(([key, value]) => `${key}=${encodeURIComponent(String(value))}`)
      .join('&')

    const currentFullPath = queryString ? `${currentPath}?${queryString}` : currentPath
    const redirectRoute = `${loginRoute}?redirect=${encodeURIComponent(currentFullPath)}`
 // toLog inpage
    uni.redirectTo({ url: redirectRoute })
  })
}
