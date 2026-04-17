package xiaozhi.modules.knowledge.rag;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import xiaozhi.common.exception.ErrorCode;
import xiaozhi.common.exception.RenException;

/**
 * Knowledge baseadapter工厂class
 * 负责createandmanagementnot 同type Knowledge baseAPIadapter
 */
@Slf4j
public class KnowledgeBaseAdapterFactory {

    // register adaptertypemapping
    private static final Map<String, Class<? extends KnowledgeBaseAdapter>> adapterRegistry = new HashMap<>();

    // adapter实examplecache
    private static final Map<String, KnowledgeBaseAdapter> adapterCache = new ConcurrentHashMap<>();

    // 最largecache实examplenumber，prevent内存泄露 (Issue 9)
    private static final int MAX_CACHE_SIZE = 50;

    static {
        // register内置adaptertype
        registerAdapter("ragflow", xiaozhi.modules.knowledge.rag.impl.RAGFlowAdapter.class);
        // 可以inthis里register更多adaptertype
    }

    /**
     * registernew adaptertype
     * 
     * @param adapterType  adaptertypeidentifier
     * @param adapterClass adapterclass
     */
    public static void registerAdapter(String adapterType, Class<? extends KnowledgeBaseAdapter> adapterClass) {
        if (adapterRegistry.containsKey(adapterType)) {
            log.warn("adaptertype '{}' already exists，willis覆盖", adapterType);
        }
        adapterRegistry.put(adapterType, adapterClass);
        log.info("registeradaptertype: {} -> {}", adapterType, adapterClass.getSimpleName());
    }

    /**
     * getadapter实example
     * 
     * @param adapterType adaptertype
     * @param config      configurationparameter
     * @return adapter实example
     */
    public static KnowledgeBaseAdapter getAdapter(String adapterType, Map<String, Object> config) {
        String cacheKey = buildCacheKey(adapterType, config);

        // 检查cacheYesNoalready exists实example
        if (adapterCache.containsKey(cacheKey)) {
            log.debug("fromcachegetadapter实example: {}", cacheKey);
            return adapterCache.get(cacheKey);
        }

        // createnew adapter实example
        KnowledgeBaseAdapter adapter = createAdapter(adapterType, config);

        // cacheadapter实example (with容量限制检查)
        if (adapterCache.size() >= MAX_CACHE_SIZE) {
            log.warn("adaptercachealready达上限 ({})，execute内存保护clear", MAX_CACHE_SIZE);
            // 简单process：directly清empty，production环境下建议use LRU
            adapterCache.clear();
        }

        adapterCache.put(cacheKey, adapter);
        log.info("create并cacheadapter实example: {}", cacheKey);

        return adapter;
    }

    /**
     * getadapter实example（无configuration）
     * 
     * @param adapterType adaptertype
     * @return adapter实example
     */
    public static KnowledgeBaseAdapter getAdapter(String adapterType) {
        return getAdapter(adapterType, null);
    }

    /**
     * get allalreadyregister adaptertype
     * 
     * @return adaptertypecollection
     */
    public static Set<String> getRegisteredAdapterTypes() {
        return adapterRegistry.keySet();
    }

    /**
     * 检查adaptertypeYesNoalreadyregister
     * 
     * @param adapterType adaptertype
     * @return YesNoalreadyregister
     */
    public static boolean isAdapterTypeRegistered(String adapterType) {
        return adapterRegistry.containsKey(adapterType);
    }

    /**
     * clearadaptercache
     */
    public static void clearCache() {
        int cacheSize = adapterCache.size();
        adapterCache.clear();
        log.info("clearadaptercache，共clear {} 个实example", cacheSize);
    }

    /**
     * 移除特定adaptertype cache
     * 
     * @param adapterType adaptertype
     */
    public static void removeCacheByType(String adapterType) {
        int removedCount = 0;
        for (String cacheKey : adapterCache.keySet()) {
            if (cacheKey.startsWith(adapterType + "@")) {
                adapterCache.remove(cacheKey);
                removedCount++;
            }
        }
        log.info("移除adaptertype '{}'  cache，共移除 {} 个实example", adapterType, removedCount);
    }

    /**
     * getadapter工厂statusinformation
     * 
     * @return statusinformation
     */
    public static Map<String, Object> getFactoryStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("registeredAdapterTypes", adapterRegistry.keySet());
        status.put("cachedAdapterCount", adapterCache.size());
        status.put("cacheKeys", adapterCache.keySet());
        return status;
    }

    /**
     * createadapter实example
     * 
     * @param adapterType adaptertype
     * @param config      configurationparameter
     * @return adapter实example
     */
    private static KnowledgeBaseAdapter createAdapter(String adapterType, Map<String, Object> config) {
        if (!adapterRegistry.containsKey(adapterType)) {
            throw new RenException(ErrorCode.RAG_ADAPTER_TYPE_NOT_SUPPORTED,
                    "not support adaptertype: " + adapterType);
        }

        try {
            Class<? extends KnowledgeBaseAdapter> adapterClass = adapterRegistry.get(adapterType);
            KnowledgeBaseAdapter adapter = adapterClass.getDeclaredConstructor().newInstance();

            // initializeadapter
            if (config != null) {
                adapter.initialize(config);

                // verificationconfiguration
                if (!adapter.validateConfig(config)) {
                    throw new RenException(ErrorCode.RAG_CONFIG_VALIDATION_FAILED,
                            "adapterconfigurationverificationfailed: " + adapterType);
                }
            }

            log.info("successcreateadapter实example: {}", adapterType);
            return adapter;

        } catch (Exception e) {
            log.error("createadapter实examplefailed: {}", adapterType, e);
            throw new RenException(ErrorCode.RAG_ADAPTER_CREATION_FAILED,
                    "createadapterfailed: " + adapterType + ", error: " + e.getMessage());
        }
    }

    /**
     * buildcache键
     * 
     * @param adapterType adaptertype
     * @param config      configurationparameter
     * @return cache键
     */
    private static String buildCacheKey(String adapterType, Map<String, Object> config) {
        if (config == null || config.isEmpty()) {
            return adapterType + "@default";
        }

        // 基于configurationparametergeneratecache键
        StringBuilder keyBuilder = new StringBuilder(adapterType + "@");

        // useconfiguration 哈希valueascache键 一部分
        int configHash = config.hashCode();
        keyBuilder.append(configHash);

        return keyBuilder.toString();
    }
}