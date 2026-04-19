// Import request modules
import admin from './module/admin.js'
import agent from './module/agent.js'
import config from './module/config.js'
import device from './module/device.js'
import dict from './module/dict.js'
import model from './module/model.js'
import ota from './module/ota.js'
import timbre from "./module/timbre.js"
import user from './module/user.js'
import voiceClone from './module/voiceClone.js'
import voiceResource from './module/voiceResource.js'
import knowledgeBase from './module/knowledgeBase.js'



/**
 * API base URL.
 * In development it reads from .env.development automatically.
 * At build time it reads from .env.production automatically.
 */
const DEV_API_SERVICE = process.env.VUE_APP_API_BASE_URL

/**
 * Return the API URL based on the current environment.
 * @returns {string}
 */
export function getServiceUrl() {
    return DEV_API_SERVICE
}

/** Request service wrapper */
export default {
    getServiceUrl,
    user,
    admin,
    agent,
    config,
    device,
    model,
    timbre,
    ota,
    dict,
    voiceResource,
    voiceClone,
    knowledgeBase
  }
