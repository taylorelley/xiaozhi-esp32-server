import type {
  ChatMessage,
  ChatSessionsResponse,
  GetSessionsParams,
} from './types'
import { http } from '@/http/request/alova'

/** * GetchatSessionlist * @param agentId AgentID * @param params PaginationParameter */
export function getChatSessions(agentId: string, params: GetSessionsParams) {
  return http.Get<ChatSessionsResponse>(`/agent/${agentId}/sessions`, {
    params,
    meta: {
      ignoreAuth: false,
      toast: false,
    },
    cacheFor: {
      expire: 0,
    },
  })
}

/** * GetChat historydetails * @param agentId AgentID * @param sessionId SessionID */
export function getChatHistory(agentId: string, sessionId: string) {
  return http.Get<ChatMessage[]>(`/agent/${agentId}/chat-history/${sessionId}`, {
    meta: {
      ignoreAuth: false,
      toast: false,
    },
    cacheFor: {
      expire: -1,
    },
  })
}

/**
 * GetAudioDownloadID
 * @param audioId AudioID
 */
export function getAudioId(audioId: string) {
  return http.Post<string>(`/agent/audio/${audioId}`, {}, {
    meta: {
      ignoreAuth: false,
      toast: false,
    },
  })
}

/** * GetAudioPlayAddress * @param downloadId DownloadID */
export function getAudioPlayUrl(downloadId: string) {
 // Based onDocument，thisisdirectlyBack of ，directlyURL
  return `/agent/play/${downloadId}`
}
