import type {
  ChatHistory,
  CreateSpeakerData,
  VoicePrint,
} from './types'
import { http } from '@/http/request/alova'

// GetVoiceprint list
export function getVoicePrintList(agentId: string) {
  return http.Get<VoicePrint[]>(`/agent/voice-print/list/${agentId}`, {
    meta: {
      ignoreAuth: false,
      toast: false,
    },
    cacheFor: {
      expire: 0,
    },
  })
}

// Get（Used forSelectVoiceprint）
export function getChatHistory(agentId: string) {
  return http.Get<ChatHistory[]>(`/agent/${agentId}/chat-history/user`, {
    meta: {
      ignoreAuth: false,
      toast: false,
    },
    cacheFor: {
      expire: 0,
    },
  })
}

// Addspeaker
export function createVoicePrint(data: CreateSpeakerData) {
  return http.Post<null>('/agent/voice-print', data, {
    meta: {
      ignoreAuth: false,
      toast: true,
    },
  })
}

// DeleteVoiceprint
export function deleteVoicePrint(id: string) {
  return http.Delete<null>(`/agent/voice-print/${id}`, {
    meta: {
      ignoreAuth: false,
      toast: true,
    },
  })
}

// UpdateVoiceprintInfo
export function updateVoicePrint(data: VoicePrint) {
  return http.Put<null>('/agent/voice-print', data, {
    meta: {
      ignoreAuth: false,
      toast: true,
    },
  })
}

// GetAudioDownloadID
export function getAudioDownloadId(audioId: string) {
  return http.Post<string>(`/agent/audio/${audioId}`, {}, {
    meta: {
      ignoreAuth: false,
      toast: false,
    },
  })
}
