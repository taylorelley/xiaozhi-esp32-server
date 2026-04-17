/* global self, workbox */

// customService Workerand of Processlogic
self.addEventListener('message', (event) => {
  if (event.data && event.data.type === 'SKIP_WAITING') {
    self.skipWaiting();
  }
});

// CDNResourcelist
const CDN_CSS = [
  'https://unpkg.com/element-ui@2.15.14/lib/theme-chalk/index.css',
  'https://cdnjs.cloudflare.com/ajax/libs/normalize/8.0.1/normalize.min.css'
];

const CDN_JS = [
  'https://unpkg.com/vue@2.6.14/dist/vue.min.js',
  'https://unpkg.com/vue-router@3.6.5/dist/vue-router.min.js',
  'https://unpkg.com/vuex@3.6.2/dist/vuex.min.js',
  'https://unpkg.com/element-ui@2.15.14/lib/index.js',
  'https://unpkg.com/axios@0.27.2/dist/axios.min.js',
  'https://unpkg.com/opus-decoder@0.7.7/dist/opus-decoder.min.js'
];

// Service Workermanifestafter
const manifest = self.__WB_MANIFEST || [];

// CheckWhether toEnableCDNmode
const isCDNEnabled = manifest.some(entry => 
  entry.url === 'cdn-mode' && entry.revision === 'enabled'
);

console.log(`Service Worker alreadyInitialize, CDNmode: ${isCDNEnabled ? 'Enable' : 'Disable'}`);

// workboxcode
importScripts('https://storage.googleapis.com/workbox-cdn/releases/7.0.0/workbox-sw.js');
workbox.setConfig({ debug: false });

// Enableworkbox
workbox.core.skipWaiting();
workbox.core.clientsClaim();

// CacheOfflinePage
const OFFLINE_URL = '/offline.html';
workbox.precaching.precacheAndRoute([
  { url: OFFLINE_URL, revision: null }
]);

// AddDoneProcess，atShowMessage
self.addEventListener('install', event => {
  if (isCDNEnabled) {
    console.log('Service Worker already，StartCacheCDNResource');
  } else {
    console.log('Service Worker already，CDNmodeDisable，CacheLocalResource');
  }
 // EnsureOfflinePageCache
  event.waitUntil(
    caches.open('offline-cache').then((cache) => {
      return cache.add(OFFLINE_URL);
    })
  );
});

// AddProcess
self.addEventListener('activate', event => {
  console.log('Service Worker already，atPage');
 // Cache
  event.waitUntil(
    caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames.filter(cacheName => {
 // current of Cache
          return cacheName.startsWith('workbox-') && !workbox.core.cacheNames.runtime.includes(cacheName);
        }).map(cacheName => {
          return caches.delete(cacheName);
        })
      );
    })
  );
});

// Addfetch，Used forCDNResourceWhether toinCache
self.addEventListener('fetch', event => {
 // hasEnableCDNmodewhenCDNResourceCache
  if (isCDNEnabled) {
    const url = new URL(event.request.url);
 // CDNResource，Whether toinCache of Info
    if ([...CDN_CSS, ...CDN_JS].includes(url.href)) {
 // Normal of fetch，Add
      console.log(`requestCDNResource: ${url.href}`);
    }
  }
});

// atCDNmodeCacheCDNResource
if (isCDNEnabled) {
 // CacheCDN of CSSResource
  workbox.routing.registerRoute(
    ({ url }) => CDN_CSS.includes(url.href),
    new workbox.strategies.CacheFirst({
      cacheName: 'cdn-stylesheets',
      plugins: [
        new workbox.expiration.ExpirationPlugin({
          maxAgeSeconds: 365 * 24 * 60 * 60, // 增加到1年缓存
          maxEntries: 10, // 最多缓存10个CSS文件
        }),
        new workbox.cacheableResponse.CacheableResponsePlugin({
          statuses: [0, 200], // 缓存成功响应
        }),
      ],
    })
  );
 // CacheCDN of JSResource
  workbox.routing.registerRoute(
    ({ url }) => CDN_JS.includes(url.href),
    new workbox.strategies.CacheFirst({
      cacheName: 'cdn-scripts',
      plugins: [
        new workbox.expiration.ExpirationPlugin({
          maxAgeSeconds: 365 * 24 * 60 * 60, // 增加到1年缓存
          maxEntries: 20, // 最多缓存20个JS文件
        }),
        new workbox.cacheableResponse.CacheableResponsePlugin({
          statuses: [0, 200], // 缓存成功响应
        }),
      ],
    })
  );
}

// Regardless ofWhether toEnableCDNmode，CacheLocalResource
workbox.routing.registerRoute(
  /\.(?:js|css|png|jpg|jpeg|svg|gif|ico|woff|woff2|eot|ttf|otf)$/,
  new workbox.strategies.StaleWhileRevalidate({
    cacheName: 'static-resources',
    plugins: [
      new workbox.expiration.ExpirationPlugin({
        maxAgeSeconds: 7 * 24 * 60 * 60, // 7天缓存
        maxEntries: 50, // 最多缓存50个文件
      }),
    ],
  })
);

// CacheHTMLPage
workbox.routing.registerRoute(
  /\.html$/,
  new workbox.strategies.NetworkFirst({
    cacheName: 'html-cache',
    plugins: [
      new workbox.expiration.ExpirationPlugin({
        maxAgeSeconds: 1 * 24 * 60 * 60, // 1天缓存
        maxEntries: 10, // 最多缓存10个HTML文件
      }),
    ],
  })
);

// OfflinePage - Use of Processmode
workbox.routing.setCatchHandler(async ({ event }) => {
 // Based onrequestTypeBack of DefaultPage
  switch (event.request.destination) {
    case 'document':
 // If it ispagerequest，BackOfflinePage
      return caches.match(OFFLINE_URL);
    default:
 // allrequestBackError
      return Response.error();
  }
}); 