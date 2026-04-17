import type { UserInfo } from '@/api/auth'
import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getUserInfo as _getUserInfo,
} from '@/api/auth'

// InitializeStatus
const userInfoState: UserInfo & { avatar?: string, token?: string } = {
  id: 0,
  username: '',
  realName: '',
  email: '',
  mobile: '',
  status: 0,
  superAdmin: 0,
  avatar: '/static/images/default-avatar.png',
  token: '',
}

export const useUserStore = defineStore(
  'userInfo',
  () => {
 // defineUser info
    const userInfo = ref<UserInfo & { avatar?: string, token?: string }>({ ...userInfoState })
    // SettingsUser info
    const setUserInfo = (val: UserInfo & { avatar?: string, token?: string }) => {
      console.log('SettingsUser info', val)
 // is thenUseDefault
      if (!val.avatar) {
        val.avatar = userInfoState.avatar
      }
      else {
        val.avatar = 'https://oss.laf.run/ukw0y1-site/avatar.jpg?feige'
      }
      userInfo.value = val
    }
    const setUserAvatar = (avatar: string) => {
      userInfo.value.avatar = avatar
      console.log('SettingsUser', avatar)
      console.log('userInfo', userInfo.value)
    }
    // DeleteUser info
    const removeUserInfo = () => {
      userInfo.value = { ...userInfoState }
      uni.removeStorageSync('userInfo')
      uni.removeStorageSync('token')
    }
    /**
     * GetUser info
     */
    const getUserInfo = async () => {
      const userData = await _getUserInfo()
      setUserInfo(userData)
      return userData
    }
    /** * Log outLog in and DeleteUser info */
    const logout = async () => {
      removeUserInfo()
    }

    return {
      userInfo,
      getUserInfo,
      setUserInfo,
      setUserAvatar,
      logout,
      removeUserInfo,
    }
  },
  {
    persist: {
      key: 'userInfo',
      serializer: {
        serialize: state => JSON.stringify(state.userInfo),
        deserialize: value => ({ userInfo: JSON.parse(value) }),
      },
    },
  },
)
