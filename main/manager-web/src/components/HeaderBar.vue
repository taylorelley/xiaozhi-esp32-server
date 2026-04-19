<template>
  <el-header class="header">
    <div class="header-container">
      <!-- -->
      <div class="header-left" @click="goHome">
        <img loading="lazy" alt="" src="@/assets/xiaozhi-logo.png" class="logo-img" />
        <img loading="lazy" alt="" :src="xiaozhiAiIcon" class="brand-img" />
      </div>

      <!-- inNavigationMenu -->
      <div class="header-center">
        <div class="equipment-management" :class="{
          'active-tab':
            $route.path === '/home' ||
            $route.path === '/role-config' ||
            $route.path === '/device-management',
        }" @click="goHome">
          <img loading="lazy" alt="" src="@/assets/header/robot.png" :style="{
            filter:
              $route.path === '/home' ||
                $route.path === '/role-config' ||
                $route.path === '/device-management'
                ? 'brightness(0) invert(1)'
                : 'None',
          }" />
          <span class="nav-text">{{ $t("header.smartManagement") }}</span>
        </div>
        <!-- UserShowVoiceclone -->
        <div v-if="!userInfo.superAdmin && featureStatus.voiceClone" class="equipment-management"
          :class="{ 'active-tab': $route.path === '/voice-clone-management' }" @click="goVoiceCloneManagement">
          <img loading="lazy" alt="" src="@/assets/header/voice.png" :style="{
            filter:
              $route.path === '/voice-clone-management'
                ? 'brightness(0) invert(1)'
                : 'None',
          }" />
          <span class="nav-text">{{ $t("header.voiceCloneManagement") }}</span>
        </div>

        <!-- managementShowVoicecloneMenu -->
        <el-dropdown v-if="userInfo.superAdmin && featureStatus.voiceClone" trigger="click" class="equipment-management more-dropdown" :class="{
          'active-tab':
            $route.path === '/voice-clone-management' ||
            $route.path === '/voice-resource-management',
        }" @visible-change="handleVoiceCloneDropdownVisibleChange">
          <span class="el-dropdown-link">
            <img loading="lazy" alt="" src="@/assets/header/voice.png" :style="{
              filter:
                $route.path === '/voice-clone-management' ||
                  $route.path === '/voice-resource-management'
                  ? 'brightness(0) invert(1)'
                  : 'None',
            }" />
            <span class="nav-text">{{ $t("header.voiceCloneManagement") }}</span>
            <i class="el-icon-arrow-down el-icon--right" :class="{ 'rotate-down': voiceCloneDropdownVisible }"></i>
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item @click.native="goVoiceCloneManagement">
              {{ $t("header.voiceCloneManagement") }}
            </el-dropdown-item>
            <el-dropdown-item @click.native="goVoiceResourceManagement">
              {{ $t("header.voiceResourceManagement") }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>

        <div v-if="userInfo.superAdmin" class="equipment-management" :class="{ 'active-tab': $route.path === '/model-config' }"
          @click="goModelConfig">
          <img loading="lazy" alt="" src="@/assets/header/model_config.png" :style="{
            filter:
              $route.path === '/model-config' ? 'brightness(0) invert(1)' : 'None',
          }" />
          <span class="nav-text">{{ $t("header.modelConfig") }}</span>
        </div>
        <div v-if="featureStatus.knowledgeBase" class="equipment-management"
          :class="{ 'active-tab': $route.path === '/knowledge-base-management' || $route.path === '/knowledge-file-upload' }"
          @click="goKnowledgeBaseManagement">
          <img loading="lazy" alt="" src="@/assets/header/knowledge_base.png" :style="{
            filter:
              $route.path === '/knowledge-base-management' || $route.path === '/knowledge-file-upload' ? 'brightness(0) invert(1)' : 'None',
          }" />
          <span class="nav-text">{{ $t("header.knowledgeBase") }}</span>
        </div>
        <el-dropdown v-if="userInfo.superAdmin" trigger="click" class="equipment-management more-dropdown" :class="{
          'active-tab':
            $route.path === '/dict-management' ||
            $route.path === '/params-management' ||
            $route.path === '/config-import' ||
            $route.path === '/provider-management' ||
            $route.path === '/server-side-management' ||
            $route.path === '/agent-template-management' ||
            $route.path === '/ota-management' ||
            $route.path === '/user-management' ||
            $route.path === '/feature-management',
        }" @visible-change="handleParamDropdownVisibleChange">
          <span class="el-dropdown-link">
            <img loading="lazy" alt="" src="@/assets/header/param_management.png" :style="{
              filter:
                $route.path === '/dict-management' ||
                  $route.path === '/params-management' ||
                  $route.path === '/provider-management' ||
                  $route.path === '/server-side-management' ||
                  $route.path === '/agent-template-management' ||
                  $route.path === '/ota-management' ||
                  $route.path === '/user-management' ||
                  $route.path === '/feature-management'
                  ? 'brightness(0) invert(1)'
                  : 'None',
            }" />
            <span class="nav-text">{{ $t("header.paramDictionary") }}</span>
            <i class="el-icon-arrow-down el-icon--right" :class="{ 'rotate-down': paramDropdownVisible }"></i>
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item @click.native="goParamManagement">
              {{ $t("header.paramManagement") }}
            </el-dropdown-item>
            <el-dropdown-item @click.native="goUserManagement">
              {{ $t("header.userManagement") }}
            </el-dropdown-item>
            <el-dropdown-item @click.native="goOtaManagement">
              {{ $t("header.otaManagement") }}
            </el-dropdown-item>
            <el-dropdown-item @click.native="goDictManagement">
              {{ $t("header.dictManagement") }}
            </el-dropdown-item>
            <el-dropdown-item @click.native="goProviderManagement">
              {{ $t("header.providerManagement") }}
            </el-dropdown-item>
            <el-dropdown-item @click.native="goAgentTemplateManagement">
              {{ $t("header.agentTemplate") }}
            </el-dropdown-item>
            <el-dropdown-item @click.native="goServerSideManagement">
              {{ $t("header.serverSideManagement") }}
            </el-dropdown-item>
            <el-dropdown-item @click.native="goFeatureManagement">
                {{ $t("header.featureManagement") }}
              </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>

      <!-- -->
      <div class="header-right">
        <div class="search-container" v-if="$route.path === '/home' && !(userInfo.superAdmin && isSmallScreen)">
          <div class="search-wrapper">
            <el-input v-model="search" :placeholder="$t('header.searchPlaceholder')" class="custom-search-input"
              @keyup.enter.native="handleSearch" @focus="showSearchHistory" @blur="hideSearchHistory" clearable
              ref="searchInput">
              <i slot="suffix" class="el-icon-search search-icon" @click="handleSearch"></i>
            </el-input>
            <!-- SearchHistory -->
            <div v-if="showHistory && searchHistory.length > 0" class="search-history-dropdown">
              <div class="search-history-header">
                <span>{{ $t("header.searchHistory") }}</span>
                <el-button type="text" size="small" class="clear-history-btn" @click="clearSearchHistory">
                  {{ $t("header.clearHistory") }}
                </el-button>
              </div>
              <div class="search-history-list">
                <div v-for="(item, index) in searchHistory" :key="index" class="search-history-item"
                  @click.stop="selectSearchHistory(item)">
                  <span class="history-text">{{ item }}</span>
                  <i class="el-icon-close clear-item-icon" @click.stop="removeSearchHistory(index)"></i>
                </div>
              </div>
            </div>
          </div>
        </div>

        <img loading="lazy" alt="" src="@/assets/home/avatar.png" class="avatar-img" @click="handleAvatarClick" />
        <span class="el-user-dropdown" @click="handleAvatarClick">
          {{ userInfo.username || "Loading..." }}
          <i class="el-icon-arrow-down el-icon--right" :class="{ 'rotate-down': userMenuVisible }"></i>
        </span>
        <el-cascader :options="userMenuOptions" trigger="click" :props="cascaderProps"
          style="width: 0px; overflow: hidden" :show-all-levels="false" @change="handleCascaderChange"
          @visible-change="handleUserMenuVisibleChange" ref="userCascader">
          <template slot-scope="{ data }">
            <span>{{ data.label }}</span>
          </template>
        </el-cascader>
      </div>
    </div>

    <!-- ModifyPasswordDialog -->
    <ChangePasswordDialog v-model="isChangePasswordDialogVisible" />
  </el-header>
</template>

<script>
import userApi from "@/apis/module/user";
import i18n, { changeLanguage } from "@/i18n";
import { mapActions, mapState } from "vuex";
import ChangePasswordDialog from "./ChangePasswordDialog.vue"; // Import the change-password dialog component
import featureManager from "@/utils/featureManager"; // Import the feature manager utility

export default {
  name: "HeaderBar",
  components: {
    ChangePasswordDialog,
  },
  props: ["devices"], // Receive device list from parent component
  data() {
    return {
      search: "",
      isChangePasswordDialogVisible: false, // Control visibility of the change-password dialog
      paramDropdownVisible: false,
      voiceCloneDropdownVisible: false,
      userMenuVisible: false, // User menu visibility state
      menuVisibleTimer: null, // Menu show timer to prevent rapid triggering
      isSmallScreen: false,
 // SearchHistory
      searchHistory: [],
      showHistory: false,
      SEARCH_HISTORY_KEY: "xiaozhi_search_history",
      MAX_HISTORY_COUNT: 3,
      // Cascader Configuration
      cascaderProps: {
        expandTrigger: "click",
        value: "value",
        label: "label",
        children: "children",
      },
    };
  },
  computed: {
    ...mapState({
      featureStatus: (state) => ({
        voiceClone: state.pubConfig.systemWebMenu?.features?.voiceClone?.enabled, // Voice-clone feature state
        knowledgeBase: state.pubConfig.systemWebMenu?.features?.knowledgeBase?.enabled, // Knowledge base feature state
      }),
      userInfo: (state) => state.userInfo,
    }),
 // Getcurrent
    currentLanguage() {
      return i18n.locale || "zh_CN";
    },
 // GetcurrentShowText
    currentLanguageText() {
      const currentLang = this.currentLanguage;
      switch (currentLang) {
        case "zh_CN":
          return this.$t("language.zhCN");
        case "zh_TW":
          return this.$t("language.zhTW");
        case "en":
          return this.$t("language.en");
        case "de":
          return this.$t("language.de");
        case "vi":
          return this.$t("language.vi");
        case "pt_BR":
          return this.$t("language.ptBR");
        default:
          return this.$t("language.zhCN");
      }
    },
 // Based oncurrentGetcorrespondingxiaozhi-aiIcon
    xiaozhiAiIcon() {
      const currentLang = this.currentLanguage;
      switch (currentLang) {
        case "zh_CN":
          return require("@/assets/xiaozhi-ai.png");
        case "zh_TW":
          return require("@/assets/xiaozhi-ai_zh_TW.png");
        case "en":
          return require("@/assets/xiaozhi-ai_en.png");
        case "de":
          return require("@/assets/xiaozhi-ai_de.png");
        case "vi":
          return require("@/assets/xiaozhi-ai_vi.png");
        case "pt_BR":
          return require("@/assets/xiaozhi-ai_en.png");
        default:
          return require("@/assets/xiaozhi-ai.png");
      }
    },
    // UserMenuOption
    userMenuOptions() {
      return [
        {
          label: this.currentLanguageText,
          value: "language",
          children: [
            {
              label: this.$t("language.zhCN"),
              value: "zh_CN",
            },
            {
              label: this.$t("language.zhTW"),
              value: "zh_TW",
            },
            {
              label: this.$t("language.en"),
              value: "en",
            },
            {
              label: this.$t("language.de"),
              value: "de",
            },
            {
              label: this.$t("language.vi"),
              value: "vi",
            },
            {
              label: this.$t("language.ptBR"),
              value: "pt_BR",
            },
          ],
        },
        {
          label: this.$t("header.changePassword"),
          value: "changePassword",
        },
        {
          label: this.$t("header.logout"),
          value: "logout",
        },
      ];
    },
  },
  async mounted() {
    this.checkScreenSize();
    window.addEventListener("resize", this.checkScreenSize);
 // fromlocalStorageLoadSearchHistory
    this.loadSearchHistory();
 // waitfeatureManagerInitializeDoneafterLoad featureStatus
    await this.loadFeatureStatus();
  },
 //Removelisten to
  beforeDestroy() {
    window.removeEventListener("resize", this.checkScreenSize);
  },
  methods: {
    goHome() {
 // Redirect toFirst
      this.$router.push("/home");
    },
    goUserManagement() {
      this.$router.push("/user-management");
    },
    goModelConfig() {
      this.$router.push("/model-config");
    },
    goKnowledgeBaseManagement() {
      this.$router.push("/knowledge-base-management");
    },
    goVoiceCloneManagement() {
      this.$router.push("/voice-clone-management");
    },
    goParamManagement() {
      this.$router.push("/params-management");
    },
    goOtaManagement() {
      this.$router.push("/ota-management");
    },
    goDictManagement() {
      this.$router.push("/dict-management");
    },
    goProviderManagement() {
      this.$router.push("/provider-management");
    },
    goServerSideManagement() {
      this.$router.push("/server-side-management");
    },
 // Redirect toVoiceResourcemanagement
    goVoiceResourceManagement() {
      this.$router.push("/voice-resource-management");
    },
    // AddDefaultRoleTemplatemanagementNavigationMethod
    goAgentTemplateManagement() {
      this.$router.push("/agent-template-management");
    },
 // Redirect tomanagement
    goFeatureManagement() {
      this.$router.push("/feature-management");
    },
 // Load featureStatus
    async loadFeatureStatus() {
 // waitfeatureManagerInitializeDone
      await featureManager.waitForInitialization();
    },
    checkScreenSize() {
      this.isSmallScreen = window.innerWidth <= 1386;
    },
 // ProcessSearch
    handleSearch() {
      const searchValue = this.search.trim();
 // IfSearchContentis，Reset
      if (!searchValue) {
        this.$emit("search-reset");
        return;
      }
 // SaveSearchHistory
      this.saveSearchHistory(searchValue);
 // Search，willSearchKeywordComponent
      this.$emit("search", searchValue);
 // SearchDoneafterinput，fromblurHideSearchHistory
      if (this.$refs.searchInput) {
        this.$refs.searchInput.blur();
      }
    },
 // ShowSearchHistory
    showSearchHistory() {
      this.showHistory = true;
    },
 // HideSearchHistory
    hideSearchHistory() {
 // Hide，
      setTimeout(() => {
        this.showHistory = false;
      }, 200);
    },
 // LoadSearchHistory
    loadSearchHistory() {
      try {
        const history = localStorage.getItem(this.SEARCH_HISTORY_KEY);
        if (history) {
          this.searchHistory = JSON.parse(history);
        }
      } catch (error) {
        console.error("LoadSearchHistoryfailed:", error);
        this.searchHistory = [];
      }
    },
 // SaveSearchHistory
    saveSearchHistory(keyword) {
      if (!keyword || this.searchHistory.includes(keyword)) {
        return;
      }
 // AddtoHistory
      this.searchHistory.unshift(keyword);
 // History
      if (this.searchHistory.length > this.MAX_HISTORY_COUNT) {
        this.searchHistory = this.searchHistory.slice(0, this.MAX_HISTORY_COUNT);
      }
 // SavetolocalStorage
      try {
        localStorage.setItem(this.SEARCH_HISTORY_KEY, JSON.stringify(this.searchHistory));
      } catch (error) {
        console.error("SaveSearchHistoryfailed:", error);
      }
    },
 // SelectSearchHistory
    selectSearchHistory(keyword) {
      this.search = keyword;
      this.handleSearch();
    },
 // RemovesingleSearchHistory
    removeSearchHistory(index) {
      this.searchHistory.splice(index, 1);
      try {
        localStorage.setItem(this.SEARCH_HISTORY_KEY, JSON.stringify(this.searchHistory));
      } catch (error) {
        console.error("UpdateSearchHistoryfailed:", error);
      }
    },
 // ClearallSearchHistory
    clearSearchHistory() {
      this.searchHistory = [];
      try {
        localStorage.removeItem(this.SEARCH_HISTORY_KEY);
      } catch (error) {
        console.error("ClearSearchHistoryfailed:", error);
      }
    },
 // ShowModifyPasswordDialog
    showChangePasswordDialog() {
      this.isChangePasswordDialogVisible = true;
 // Add：ShowModifyPasswordDialogafterResetUserMenuStatus
      this.userMenuVisible = false;
    },
    // Log outLog in
    async handleLogout() {
      try {
 // Call Vuex of logout action
        await this.logout();
        this.$message.success({
          message: this.$t("message.success"),
          showClose: true,
        });
      } catch (error) {
        console.error("Log outLog infailed:", error);
        this.$message.error({
          message: this.$t("message.error"),
          showClose: true,
        });
      }
    },
 // listen toParameterDictionaryMenu of Statuschange
    handleParamDropdownVisibleChange(visible) {
      this.paramDropdownVisible = visible;
    },
 // listen toVoicecloneMenu of Statuschange
    handleVoiceCloneDropdownVisibleChange(visible) {
      this.voiceCloneDropdownVisible = visible;
    },
 // atdatainAddkeyUsed forre-Component
 // Process Cascader Selectchange
    handleCascaderChange(value) {
      if (!value || value.length === 0) {
        return;
      }

      const action = value[value.length - 1];
 // ProcessSwitch
      if (value.length === 2 && value[0] === "language") {
        this.changeLanguage(action);
      } else {
 // ProcessAction
        switch (action) {
          case "changePassword":
            this.showChangePasswordDialog();
            break;
          case "logout":
            this.handleLogout();
            break;
        }
      }
 // ActionDoneafterClearSelect
      setTimeout(() => {
        this.completeResetCascader();
      }, 300);
    },
 // Switch
    changeLanguage(lang) {
      changeLanguage(lang);
      this.$message.success({
        message: this.$t("message.success"),
        showClose: true,
      });
 // Add：SwitchafterResetUserMenuStatus
      this.userMenuVisible = false;
    },
 // ResetSelect
    completeResetCascader() {
      if (this.$refs.userCascader) {
        try {
 // all of MethodClearSelect
 // 1. UseComponent of clearValueMethod
          if (this.$refs.userCascader.clearValue) {
            this.$refs.userCascader.clearValue();
          }
 // 2. directlyClearProperty
          if (this.$refs.userCascader.$data) {
            this.$refs.userCascader.$data.selectedPaths = [];
            this.$refs.userCascader.$data.displayLabels = [];
            this.$refs.userCascader.$data.inputValue = "";
            this.$refs.userCascader.$data.checkedValue = [];
            this.$refs.userCascader.$data.showAllLevels = false;
          }

          // 3. ActionDOMClearSelectedStatus
          const menuElement = this.$refs.userCascader.$refs.menu;
          if (menuElement && menuElement.$el) {
            const activeItems = menuElement.$el.querySelectorAll(
              ".el-cascader-node.is-active"
            );
            activeItems.forEach((item) => item.classList.remove("is-active"));

            const checkedItems = menuElement.$el.querySelectorAll(
              ".el-cascader-node.is-checked"
            );
            checkedItems.forEach((item) => item.classList.remove("is-checked"));
          }

          console.log("Cascader values cleared");
        } catch (error) {
          console.error("ClearSelectvaluefailed:", error);
        }
      }
    },
 // cascaderMenu
    handleAvatarClick() {
      if (this.$refs.userCascader) {
 // SwitchMenuStatus
        this.userMenuVisible = !this.userMenuVisible;
 // MenuCollapsewhenClearSelectvalue
        if (!this.userMenuVisible) {
          this.completeResetCascader();
        }
 // directlySettingsMenu of Status
        try {
 // UsetoggleDropDownVisibleMethod
          this.$refs.userCascader.toggleDropDownVisible(this.userMenuVisible);
        } catch (error) {
 // IftoggleMethodfailed，directlySettingsProperty
          if (this.$refs.userCascader.$refs.menu) {
            this.$refs.userCascader.$refs.menu.showMenu(this.userMenuVisible);
          } else {
            console.error("Cannot access menu component");
          }
        }
      }
    },
 // ProcessUserMenuchange
    handleUserMenuVisibleChange(visible) {
      if (this.menuVisibleTimer) return;
      this.menuVisibleTimer = setTimeout(() => {
        this.userMenuVisible = visible;
        clearTimeout(this.menuVisibleTimer);
        this.menuVisibleTimer = null;
      }, 100);
 // IfMenuClose，ClearSelectvalue
      if (!visible) {
        this.completeResetCascader();
      }
    },
 // Use mapActions Vuex of logout action
    ...mapActions(["logout"]),
  },
};
</script>

<style lang="scss" scoped>
.header {
  background: #f6fcfe66;
  border: 1px solid #fff;
  height: 63px !important;
  min-width: 900px;
  /* Settings */
  overflow: visible;
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 10px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 120px;
  cursor: pointer;
}

.logo-img {
  width: 42px;
  height: 42px;
}

.brand-img {
  height: 20px;
}

.header-center {
  display: flex;
  align-items: center;
  gap: 25px;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 7px;
  min-width: 300px;
  justify-content: flex-end;
}

.equipment-management {
  height: 30px;
  border-radius: 15px;
  background: #deeafe;
  display: flex;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
  gap: 7px;
  color: #3d4566;
  margin-left: 1px;
  align-items: center;
  transition: all 0.3s ease;
  cursor: pointer;
  flex-shrink: 0;
  /* NavigationButton */
  padding: 0 15px;
  position: relative;
}

.equipment-management.active-tab {
  background: #5778ff !important;
  color: #fff !important;
}

.equipment-management img {
  width: 15px;
  height: 13px;
}

.search-container {
  margin-right: 5px;
  flex: 0.9;
  min-width: 60px;
  max-width: none;
}

.search-wrapper {
  position: relative;
}

.search-history-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #e4e6ef;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1000;
  margin-top: 2px;
}

.search-history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 12px;
  color: #909399;
}

.clear-history-btn {
  color: #909399;
  font-size: 11px;
  padding: 0;
  height: auto;
}

.clear-history-btn:hover {
  color: #606266;
}

.search-history-list {
  max-height: 200px;
  overflow-y: auto;
}

.search-history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 12px;
  color: #606266;
}

.search-history-item:hover {
  background-color: #f5f7fa;
}

.clear-item-icon {
  font-size: 10px;
  color: #909399;
  visibility: hidden;
}
.more-dropdown {
  padding: 0;
}
.more-dropdown .el-dropdown-link {
  display: flex;
  align-items: center;
  gap: 7px;
  height: 100%;
  padding: 0 15px;
}

.search-history-item:hover .clear-item-icon {
  visibility: visible;
}

.clear-item-icon:hover {
  color: #ff4949;
}

.custom-search-input>>>.el-input__inner {
  height: 18px;
  border-radius: 9px;
  background-color: #fff;
  border: 1px solid #e4e6ef;
  padding-left: 8px;
  font-size: 9px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  width: 100%;
}

.search-icon {
  cursor: pointer;
  color: #909399;
  margin-right: 3px;
  font-size: 9px;
  line-height: 18px;
}

.custom-search-input::v-deep .el-input__suffix-inner {
  display: flex;
  align-items: center;
  height: 100%;
}

.avatar-img {
  width: 21px;
  height: 21px;
  flex-shrink: 0;
  cursor: pointer;
}
.el-user-dropdown {
  cursor: pointer;
}

/* NavigationTextStyle - in */
.nav-text {
  white-space: normal;
  text-align: center;
  max-width: 80px;
  line-height: 1.2;
}

/* Response */
@media (max-width: 1200px) {
  .header-center {
    gap: 14px;
  }

  .equipment-management {
    min-width: 80px;
    font-size: 10px;
  }
}

.equipment-management.more-dropdown {
  position: relative;
}

.equipment-management.more-dropdown .el-dropdown-menu {
  position: absolute;
  right: 0;
  min-width: 120px;
  margin-top: 5px;
}

.el-dropdown-menu__item {
  min-width: 60px;
  padding: 8px 20px;
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

/* AddStyle */
.rotate-down {
  transform: rotate(180deg);
  transition: transform 0.3s ease;
}

.el-icon-arrow-down {
  transition: transform 0.3s ease;
}
</style>
