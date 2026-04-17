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
  chatType: 1 | 2 | 3 // 1 = user, 2 = AI, 3 = parameter description
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
  data: string // Audio download ID
}
