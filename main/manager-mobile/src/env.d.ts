/// <reference types="vite/client" />
/// <reference types="vite-svg-loader" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'

  const component: DefineComponent<{}, {}, any>
  export default component
}

interface ImportMetaEnv {
  /** Title，applicationName */
  readonly VITE_APP_TITLE: string
  /** server */
  readonly VITE_SERVER_PORT: string
  /** afterAPIAddress */
  readonly VITE_SERVER_BASEURL: string
  /** H5Whether toneeds */
  readonly VITE_APP_PROXY: 'true' | 'false'
  /** H5Whether toneeds，needs of has */
  readonly VITE_APP_PROXY_PREFIX: string // Usually /api
  /** UploadImageAddress */
  readonly VITE_UPLOAD_BASEURL: string
  /** Whether toClearconsole */
  readonly VITE_DELETE_CONSOLE: string
 // Variable...
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

declare const __VITE_APP_PROXY__: 'true' | 'false'
declare const __UNI_PLATFORM__: 'app' | 'h5' | 'mp-alipay' | 'mp-baidu' | 'mp-kuaishou' | 'mp-lark' | 'mp-qq' | 'mp-tiktok' | 'mp-weixin' | 'mp-xiaochengxu'
