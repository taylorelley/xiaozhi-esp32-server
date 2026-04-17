/** * Cachetool - Used forCheckCDNResourceWhether toalreadyService WorkerCache */

/** * GetallService WorkerCache of Name * @returns {Promise<string[]>} CacheNamelist */
export const getCacheNames = async () => {
  if (!('caches' in window)) {
    return [];
  }
  
  try {
    return await caches.keys();
  } catch (error) {
    console.error('GetCacheNamefailed:', error);
    return [];
  }
};

/** * GetCachein of allURL * @param {string} cacheName CacheName * @returns {Promise<string[]>} Cache of URLlist */
export const getCacheUrls = async (cacheName) => {
  if (!('caches' in window)) {
    return [];
  }
  
  try {
    const cache = await caches.open(cacheName);
    const requests = await cache.keys();
    return requests.map(request => request.url);
  } catch (error) {
    console.error(`GetCache ${cacheName} of URLfailed:`, error);
    return [];
  }
};

/** * CheckURLWhether toalreadyCache * @param {string} url Check of URL * @returns {Promise<boolean>} Whether toalreadyCache */
export const isUrlCached = async (url) => {
  if (!('caches' in window)) {
    return false;
  }
  
  try {
    const cacheNames = await getCacheNames();
    for (const cacheName of cacheNames) {
      const cache = await caches.open(cacheName);
      const match = await cache.match(url);
      if (match) {
        return true;
      }
    }
    return false;
  } catch (error) {
    console.error(`CheckURL ${url} Whether toCachefailed:`, error);
    return false;
  }
};

/** * GetcurrentPageallCDNResource of CacheStatus * @returns {Promise<Object>} CacheStatusObject */
export const checkCdnCacheStatus = async () => {
 // fromCDNCacheinResource
  const cdnCaches = ['cdn-stylesheets', 'cdn-scripts'];
  const results = {
    css: [],
    js: [],
    totalCached: 0,
    totalNotCached: 0
  };
  
  for (const cacheName of cdnCaches) {
    try {
      const urls = await getCacheUrls(cacheName);
 // CSSandJSResource
      for (const url of urls) {
        if (url.endsWith('.css')) {
          results.css.push({ url, cached: true });
        } else if (url.endsWith('.js')) {
          results.js.push({ url, cached: true });
        }
        results.totalCached++;
      }
    } catch (error) {
      console.error(`Get ${cacheName} CacheInfofailed:`, error);
    }
  }
  
  return results;
};

/** * ClearallService WorkerCache * @returns {Promise<boolean>} Whether tosuccessfulClear */
export const clearAllCaches = async () => {
  if (!('caches' in window)) {
    return false;
  }
  
  try {
    const cacheNames = await getCacheNames();
    for (const cacheName of cacheNames) {
      await caches.delete(cacheName);
    }
    return true;
  } catch (error) {
    console.error('ClearallCachefailed:', error);
    return false;
  }
};

/** * willCacheStatusto */
export const logCacheStatus = async () => {
  console.group('Service Worker cache status');
  
  const cacheNames = await getCacheNames();
  console.log('already of Cache:', cacheNames);
  
  for (const cacheName of cacheNames) {
    const urls = await getCacheUrls(cacheName);
    console.group(`Cache: ${cacheName} (${urls.length} items)`);
    urls.forEach(url => console.log(url));
    console.groupEnd();
  }
  
  console.groupEnd();
  return cacheNames.length > 0;
}; 