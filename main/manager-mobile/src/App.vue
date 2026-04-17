<script setup lang="ts">
import { onHide, onLaunch, onShow } from '@dcloudio/uni-app'
import { watch, onMounted } from 'vue'
import { usePageAuth } from '@/hooks/usePageAuth'
import { useConfigStore } from '@/store'
import { t } from '@/i18n'
import { useLangStore } from '@/store/lang'
import 'abortcontroller-polyfill/dist/abortcontroller-polyfill-only'

usePageAuth()

const configStore = useConfigStore()
const langStore = useLangStore()

onLaunch(() => {
  console.log('App Launch')
  // GetPublicConfiguration
  configStore.fetchPublicConfig().catch((error) => {
    console.error('GetPublicConfigurationfailed:', error)
  })
})
onShow(() => {
  console.log('App Show')
 // UsesetTimeout，EnsuretabBaralreadyInitialize
  setTimeout(() => {
    updateTabBarText()
  }, 100)
})

// UpdatetabBarText
function updateTabBarText() {
  try {
 // SettingsFirsttabBarText
    uni.setTabBarItem({
      index: 0,
      text: t('tabBar.home'),
      success: () => {},
      fail: (err) => {
        console.log('SettingsFirsttabBarTextfailed:', err)
      }
    })
 // Settingsnetwork configurationtabBarText
    uni.setTabBarItem({
      index: 1,
      text: t('tabBar.deviceConfig'),
      success: () => {},
      fail: (err) => {
        console.log('Settingsnetwork configurationtabBarTextfailed:', err)
      }
    })
 // SettingstabBarText
    uni.setTabBarItem({
      index: 2,
      text: t('tabBar.settings'),
      success: () => {},
      fail: (err) => {
        console.log('SettingstabBarTextfailed:', err)
      }
    })
  } catch (error) {
    console.log('UpdatetabBarTextwhen:', error)
  }
}
// listen toSwitch
onMounted(() => {
 // listen tochange，whenUpdatetabBarText
  watch(() => langStore.currentLang, () => {
    console.log('alreadySwitch，UpdatetabBarText')
 // SwitchafterUpdatetabBarText
    updateTabBarText()
  })
})

onHide(() => {
  console.log('App Hide')
})
</script>

<style lang="scss">
swiper,
scroll-view {
  flex: 1;
  height: 100%;
  overflow: hidden;
}

image {
  width: 100%;
  height: 100%;
  vertical-align: middle;
}
</style>
