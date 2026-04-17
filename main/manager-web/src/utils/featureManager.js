//Configurationtool
import Api from "@/apis/api";
import store from "@/store";

class FeatureManager {
    constructor() {
        this.defaultFeatures = {
            voiceprintRecognition: {
                name: 'feature.voiceprintRecognition.name',
                enabled: false,
                description: 'feature.voiceprintRecognition.description'
            },
            voiceClone: {
                name: 'feature.voiceClone.name',
                enabled: false,
                description: 'feature.voiceClone.description'
            },
            knowledgeBase: {
                name: 'feature.knowledgeBase.name',
                enabled: false,
                description: 'feature.knowledgeBase.description'
            },
            mcpAccessPoint: {
                name: 'feature.mcpAccessPoint.name',
                enabled: false,
                description: 'feature.mcpAccessPoint.description'
            },
            vad: {
                name: 'feature.vad.name',
                enabled: false,
                description: 'feature.vad.description'
            },
            asr: {
                name: 'feature.asr.name',
                enabled: false,
                description: 'feature.asr.description'
            }
        };
        this.currentFeatures = { ...this.defaultFeatures }; // Current in-memory configuration
        this.initialized = false;
        this.initPromise = null;
    }

    /** * waitInitializeDone */
    async waitForInitialization() {
        if (!this.initPromise) {
            this.initPromise = this.init();
        }
        await this.initPromise;
        return this.initialized;
    }

    /** * InitializeConfiguration */
    async init() {
        try {
 // frompub-configAPIGetConfiguration
            const config = await this.getConfigFromPubConfig();
            if (config) {
                this.currentFeatures = { ...config }; // Persist to memory
                this.initialized = true;
                return;
            }
        } catch (error) {
            console.warn('frompub-configAPIGetConfigurationfailed:', error);
        }
 // pub-configAPIfailed，UseDefaultConfiguration
        this.currentFeatures = { ...this.defaultFeatures }; // Persist default config to memory
        this.initialized = true;
    }

    /**
     * UpdateconfigCache
     */
    updateConfigCache(config) {
        store.commit('setPubConfig', config);
        localStorage.setItem('pubConfig', JSON.stringify(config));
    }

    /** * frompub-configAPIGetConfiguration */
    async getConfigFromPubConfig() {
        return new Promise((resolve) => {
 // directlyCallpub-configAPIGetConfiguration
            Api.user.getPubConfig((result) => {
 // CheckBackResult of 
                if (result && result.status === 200) {
 // CheckWhether tohasdataField
                    if (result.data) {
                        const configCache = result.data.data || {};
 // CheckWhether tohascodeField，IfhasthencodeCheck
                        if (result.data.code !== undefined) {
                            if (result.data.code === 0 && result.data.data && result.data.data.systemWebMenu) {
                                try {
                                    let config;
                                    if (typeof result.data.data.systemWebMenu === 'string') {
 // If it isString，needsParseJSON
                                        config = JSON.parse(result.data.data.systemWebMenu);
                                    } else {
 // IfalreadyisObject，directlyUse
                                        config = result.data.data.systemWebMenu;
                                    }
 // CheckConfigurationinWhether toincludesfeaturesObject
                                    if (config && config.features) {
 // EnsureknowledgeBaseatConfiguration
                                        if (!config.features.knowledgeBase) {
                                            console.warn('ConfigurationinknowledgeBase，andDefaultConfiguration');
                                            config.features = { ...this.defaultFeatures, ...config.features };
                                        }
                                        resolve(config.features);
                                    } else {
                                        console.warn('ConfigurationinfeaturesObject，UseDefaultConfiguration');
                                        resolve(this.defaultFeatures);
                                    }
                                    configCache.systemWebMenu = config;
                                } catch (error) {
                                    console.warn('ProcesssystemWebMenuConfigurationfailed:', error);
                                    resolve(null);
                                }
                            } else {
                                console.warn('APIBackcodeis0orData，UseDefaultConfiguration');
                                resolve(null);
                            }
                        } else {
 // IfhascodeField，directlyChecksystemWebMenu
                            if (result.data && result.data.systemWebMenu) {
                                try {
                                    let config;
                                    if (typeof result.data.systemWebMenu === 'string') {
 // If it isString，needsParseJSON
                                        config = JSON.parse(result.data.systemWebMenu);
                                    } else {
 // IfalreadyisObject，directlyUse
                                        config = result.data.systemWebMenu;
                                    }
 // CheckConfigurationinWhether toincludesfeaturesObject
                                    if (config && config.features) {
 // EnsureknowledgeBaseatConfiguration
                                        if (!config.features.knowledgeBase) {
                                            console.warn('ConfigurationinknowledgeBase，andDefaultConfiguration');
                                            config.features = { ...this.defaultFeatures, ...config.features };
                                        }
                                        resolve(config.features);
                                    } else {
                                        console.warn('ConfigurationinfeaturesObject，UseDefaultConfiguration');
                                        resolve(this.defaultFeatures);
                                    }
                                    configCache.systemWebMenu = config;
                                } catch (error) {
                                    console.warn('ProcesssystemWebMenuConfigurationfailed:', error);
                                    resolve(null);
                                }
                            } else {
                                console.warn('APIBacksystemWebMenuData，UseDefaultConfiguration');
                                resolve(null);
                            }
                        }
                        this.updateConfigCache(configCache)
                    } else {
                        console.warn('APIBackDataindataField，UseDefaultConfiguration');
                        resolve(null);
                    }
                } else {
                    console.warn('pub-configAPICallfailed，UseDefaultConfiguration');
                    resolve(null);
                }
            });
        });
    }

    /** * GetcurrentConfiguration */
    getCurrentConfig() {
 // Backin of currentConfiguration
        return this.currentFeatures;
    }

    /** * SaveConfigurationtobackendAPI */
    async saveConfig(config) {
        try {
 // Updatein of Configuration
            this.currentFeatures = { ...config };
 // SavetobackendAPI
            this.saveConfigToAPI(config).catch(error => {
                console.warn('SaveConfigurationtoAPIfailed:', error);
            }).finally(() => {
                this.init()
            });
 // Configuration
            window.dispatchEvent(new CustomEvent('featureConfigChanged', {
                detail: config
            }));
        } catch (error) {
            console.error('SaveConfigurationfailed:', error);
        }
    }

    /** * SaveConfigurationtobackendAPI */
    async saveConfigToAPI(config) {
        return new Promise((resolve) => {
 // directlyUsealready of ID（600）UpdateParameter
            Api.admin.updateParam(
                {
                    id: 600,
                    paramCode: 'system-web.menu',
                    paramValue: JSON.stringify({
                        features: config,
                        groups: {
                            featureManagement: ["voiceprintRecognition", "voiceClone", "knowledgeBase", "mcpAccessPoint"],
                            voiceManagement: ["vad", "asr"]
                        }
                    }),
                    valueType: 'json',
                    remark: 'System feature menu configuration'
                },
                (updateResult) => {
                    if (updateResult.code === 0) {
                        resolve();
                    } else {
 // IfFailed to update，isParameteratorError，SavetolocalStorage
                        console.warn('UpdateParameterfailed:', updateResult.msg);
                        resolve(); // Do not block save to localStorage
                    }
                },
                (error) => {
                    console.warn('UpdateParameterfailed:', error);
                    resolve(); // Do not block save to localStorage
                }
            );
        });
    }



    /** * GetallConfiguration */
    getAllFeatures() {
        return this.getCurrentConfig();
    }

    /** * Get of ConfigurationObject（Used forFirstComponent） */
    getConfig() {
        const features = this.getAllFeatures();
        return {
            voiceprintRecognition: features.voiceprintRecognition?.enabled || false,
            voiceClone: features.voiceClone?.enabled || false,
            knowledgeBase: features.knowledgeBase?.enabled || false,
            mcpAccessPoint: features.mcpAccessPoint?.enabled || false,
            vad: features.vad?.enabled || false,
            asr: features.asr?.enabled || false
        };
    }

    /** * Get of Status */
    getFeatureStatus(featureKey) {
        const features = this.getAllFeatures();
        return features[featureKey]?.enabled || false;
    }

    /** * SettingsStatus */
    setFeatureStatus(featureKey, enabled) {
        const features = this.getAllFeatures();
        if (features[featureKey]) {
            features[featureKey].enabled = enabled;
            this.saveConfig(features);
            return true;
        }
        return false;
    }

    /** * Enable */
    enableFeature(featureKey) {
        return this.setFeatureStatus(featureKey, true);
    }

    /** * Disable */
    disableFeature(featureKey) {
        return this.setFeatureStatus(featureKey, false);
    }

    /** * SwitchStatus */
    toggleFeature(featureKey) {
        const currentStatus = this.getFeatureStatus(featureKey);
        return this.setFeatureStatus(featureKey, !currentStatus);
    }

    /** * ResetallisDefaultStatus */
    resetToDefault() {
        this.saveConfig(this.defaultFeatures);
    }

    /** * batchUpdateStatus */
    updateFeatures(featureUpdates) {
        const features = this.getAllFeatures();
        Object.keys(featureUpdates).forEach(featureKey => {
            if (features[featureKey]) {
                features[featureKey].enabled = featureUpdates[featureKey];
            }
        });
        this.saveConfig(features);
    }

    /** * GetalreadyEnable of list */
    getEnabledFeatures() {
        const features = this.getAllFeatures();
        return Object.keys(features).filter(key => features[key].enabled);
    }

    /** * CheckWhether toEnable */
    isFeatureEnabled(featureKey) {
        return this.getFeatureStatus(featureKey);
    }
}

// Createinstance
const featureManager = new FeatureManager();

export default featureManager;