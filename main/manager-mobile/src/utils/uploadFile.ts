import { getEnvBaseUrl } from './index'
import { toast } from './toast'

/** * FileUploadFunctionUse * @example * const { loading, error, data, progress, run } = useUpload<IUploadResult>( * uploadUrl, * {}, * { * maxSize: 5, // 5MB * sourceType: ['album'], // fromSelect * onProgress: (p) => console.log(`Uploadprogress：${p}%`), * onSuccess: (res) => console.log('Uploaded successfully', res), * onError: (err) => console.error('Failed to upload', err), * }, * ) */

/** * UploadFile of URLConfiguration */
export const uploadFileUrl = {
  /** UserUploadAddress（current of BaseURL） */
  get USER_AVATAR() {
    return `${getEnvBaseUrl()}/user/avatar`
  },
}

/** * FileUploadFunction（directlyFilePath） * @param url UploadAddress * @param filePath LocalFilePath * @param formData FormData * @param options UploadOption */
export function useFileUpload<T = string>(url: string, filePath: string, formData: Record<string, any> = {}, options: Omit<UploadOptions, 'sourceType' | 'sizeType' | 'count'> = {}) {
  return useUpload<T>(
    url,
    formData,
    {
      ...options,
      sourceType: ['album'],
      sizeType: ['original'],
    },
    filePath,
  )
}

export interface UploadOptions {
  /** Select of Image，Defaultis1 */
  count?: number
  /** of Image of ，original-，compressed- */
  sizeType?: Array<'original' | 'compressed'>
  /** SelectImage of ，album-，camera- */
  sourceType?: Array<'album' | 'camera'>
  /** Filesize，：MB */
  maxSize?: number //
  /** Uploadprogresscallback */
  onProgress?: (progress: number) => void
  /** Uploaded successfullycallback */
  onSuccess?: (res: Record<string, any>) => void
  /** Failed to uploadcallback */
  onError?: (err: Error | UniApp.GeneralCallbackResult) => void
  /** UploadDonecallback（Regardless ofsuccessfulfailed） */
  onComplete?: () => void
}

/** * FileUploadFunction * @template T Uploaded successfullyafterBack of DataType * @param url UploadAddress * @param formData of FormData * @param options UploadOption * @returns UploadStatusandObject */
export function useUpload<T = string>(url: string, formData: Record<string, any> = {}, options: UploadOptions = {},
  /** directlyFilePath，Select */
  directFilePath?: string) {
  /** UploadinStatus */
  const loading = ref(false)
  /** UploadErrorStatus */
  const error = ref(false)
  /** Uploaded successfullyafter of ResponseData */
  const data = ref<T>()
  /** Uploadprogress（0-100） */
  const progress = ref(0)

  /** UploadOption，SettingsDefaultvalue */
  const {
    /** Select of Image */
    count = 1,
    /** of Image of */
    sizeType = ['original', 'compressed'],
    /** SelectImage of */
    sourceType = ['album', 'camera'],
    /** Filesize（MB） */
    maxSize = 10,
    /** progresscallback */
    onProgress,
    /** successfulcallback */
    onSuccess,
    /** failedcallback */
    onError,
    /** Donecallback */
    onComplete,
  } = options

  /** * CheckFilesizeWhether to * @param size Filesize（） * @returns Whether toCheck */
  const checkFileSize = (size: number) => {
    const sizeInMB = size / 1024 / 1024
    if (sizeInMB > maxSize) {
      toast.warning(`文件大小不能超过${maxSize}MB`)
      return false
    }
    return true
  }
  /** * FileSelectandUpload * Based onUse of Select： * - Use chooseMedia * - Use chooseImage */
  const run = () => {
    if (directFilePath) {
 // directlyUse of FilePath
      loading.value = true
      progress.value = 0
      uploadFile<T>({
        url,
        tempFilePath: directFilePath,
        formData,
        data,
        error,
        loading,
        progress,
        onProgress,
        onSuccess,
        onError,
        onComplete,
      })
      return
    }

    // #ifdef MP-WEIXIN
 // Use chooseMedia API
    uni.chooseMedia({
      count,
      mediaType: ['image'], // 仅支持图片类型
      sourceType,
      success: (res) => {
        const file = res.tempFiles[0]
 // CheckFilesizeWhether to
        if (!checkFileSize(file.size))
          return

        // StartUpload
        loading.value = true
        progress.value = 0
        uploadFile<T>({
          url,
          tempFilePath: file.tempFilePath,
          formData,
          data,
          error,
          loading,
          progress,
          onProgress,
          onSuccess,
          onError,
          onComplete,
        })
      },
      fail: (err) => {
        console.error('SelectFilefailed:', err)
        error.value = true
        onError?.(err)
      },
    })
    // #endif

    // #ifndef MP-WEIXIN
 // Use chooseImage API
    uni.chooseImage({
      count,
      sizeType,
      sourceType,
      success: (res) => {
        console.log('SelectImagesuccessful:', res)

        // StartUpload
        loading.value = true
        progress.value = 0
        uploadFile<T>({
          url,
          tempFilePath: res.tempFilePaths[0],
          formData,
          data,
          error,
          loading,
          progress,
          onProgress,
          onSuccess,
          onError,
          onComplete,
        })
      },
      fail: (err) => {
        console.error('SelectImagefailed:', err)
        error.value = true
        onError?.(err)
      },
    })
    // #endif
  }

  return { loading, error, data, progress, run }
}

/** * FileUploadOptionAPI * @template T Uploaded successfullyafterBack of DataType */
interface UploadFileOptions<T> {
  /** UploadAddress */
  url: string
  /** whenFilePath */
  tempFilePath: string
  /** of FormData */
  formData: Record<string, any>
  /** Uploaded successfullyafter of ResponseData */
  data: Ref<T | undefined>
  /** UploadErrorStatus */
  error: Ref<boolean>
  /** UploadinStatus */
  loading: Ref<boolean>
  /** Uploadprogress（0-100） */
  progress: Ref<number>
  /** Uploadprogresscallback */
  onProgress?: (progress: number) => void
  /** Uploaded successfullycallback */
  onSuccess?: (res: Record<string, any>) => void
  /** Failed to uploadcallback */
  onError?: (err: Error | UniApp.GeneralCallbackResult) => void
  /** UploadDonecallback */
  onComplete?: () => void
}

/** * FileUpload * @template T Uploaded successfullyafterBack of DataType * @param options UploadOption */
function uploadFile<T>({
  url,
  tempFilePath,
  formData,
  data,
  error,
  loading,
  progress,
  onProgress,
  onSuccess,
  onError,
  onComplete,
}: UploadFileOptions<T>) {
  try {
 // CreateUpload
    const uploadTask = uni.uploadFile({
      url,
      filePath: tempFilePath,
      name: 'file', // 文件对应的 key
      formData,
      header: {
 // H5needsmanualSettingsContent-Type，BrowserProcessmultipartFormat
        // #ifndef H5
        'Content-Type': 'multipart/form-data',
        // #endif
      },
 // EnsureFileName
      success: (uploadFileRes) => {
        console.log('UploadFilesuccessful:', uploadFileRes)
        try {
 // ParseResponseData
          const { data: _data } = JSON.parse(uploadFileRes.data)
          // Uploaded successfully
          data.value = _data as T
          onSuccess?.(_data)
        }
        catch (err) {
 // ResponseParseError
          console.error('ParseUploadResponsefailed:', err)
          error.value = true
          onError?.(new Error('上传响应解析失败'))
        }
      },
      fail: (err) => {
        // UploadRequest failed
        console.error('UploadFilefailed:', err)
        error.value = true
        onError?.(err)
      },
      complete: () => {
 // Regardless ofsuccessfulfailed
        loading.value = false
        onComplete?.()
      },
    })
 // listen toUploadprogress
    uploadTask.onProgressUpdate((res) => {
      progress.value = res.progress
      onProgress?.(res.progress)
    })
  }
  catch (err) {
 // CreateUploadfailed
    console.error('CreateUploadfailed:', err)
    error.value = true
    loading.value = false
    onError?.(new Error('创建上传任务失败'))
  }
}
