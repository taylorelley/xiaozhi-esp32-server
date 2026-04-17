// VoiceprintInfoResponseType
export interface VoicePrint {
  id: string
  audioId: string
  sourceName: string
  introduce: string
  createDate: string
}

// Type
export interface ChatHistory {
  content: string
  audioId: string
}

// CreatespeakerDataType
export interface CreateSpeakerData {
  agentId: string
  audioId: string
  sourceName: string
  introduce: string
}

// ResponseType
export interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
}
