/** * tabbar Status， storageSync RefreshBrowserwhenat of tabbar Page * UsereactiveStatus，is pinia GlobalStatus */
export const tabbarStore = reactive({
  curIdx: uni.getStorageSync('app-tabbar-index') || 0,
  setCurIdx(idx: number) {
    this.curIdx = idx
    uni.setStorageSync('app-tabbar-index', idx)
  },
})
