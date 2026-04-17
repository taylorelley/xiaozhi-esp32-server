// ResponseFormat
export interface IResponse<T = any> {
  code: number | string
  data: T
  msg: string
  status: string | number
}

// PaginationRequest parameters
export interface PageParams {
  page: number
  pageSize: number
  [key: string]: any
}

// PaginationResponseData
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}
