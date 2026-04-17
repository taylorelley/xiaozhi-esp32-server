// chatSessionlist
export interface ChatSession {
  sessionId: string
  createdAt: string
  chatCount: number
  title: string
}

// chatSessionlistResponse
export interface ChatSessionsResponse {
  total: number
  list: ChatSession[]
}

// chatMessage
export interface ChatMessage {
  createdAt: string
  chatType: 1 | 2 | 3 // 1是用户，2是AI，3是参数说明
  content: string
  audioId: string | null
  macAddress: string
}

// UserMessageContent（needsParseJSON）
export interface UserMessageContent {
  speaker: string
  content: string
}

// GetchatSessionlistParameter
export interface GetSessionsParams {
  page: number
  limit: number
}

// AudioPlay
export interface AudioResponse {
  data: string // 音频下载ID
}
