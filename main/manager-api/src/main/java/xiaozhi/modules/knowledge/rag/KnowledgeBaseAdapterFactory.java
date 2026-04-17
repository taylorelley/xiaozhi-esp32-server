package xiaozhi.modules.knowledge.rag;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import xiaozhi.common.exception.ErrorCode;
import xiaozhi.common.exception.RenException;

/**
 * Knowledge baseadapterfactoryclass
 * responsiblecreateandmanagementnot sametype Knowledge baseAPIadapter
 */
@Slf4j
public class KnowledgeBaseAdapterFactory {

    // register adaptertypemapping
    private static final Map<String, Class<? extends KnowledgeBaseAdapter>> adapterRegistry = new HashMap<>();

    // adapterexamplecache
    private static final Map<String, KnowledgeBaseAdapter> adapterCache = new ConcurrentHashMap<>();

    // mostlargecacheexamplenumber，preventinternalstoreleak (Issue 9)
    private static final int MAX_CACHE_SIZE = 50;

    static {
        // registerinternalsetadaptertype
        registerAdapter("ragflow", xiaozhi.modules.knowledge.rag.impl.RAGFlowAdapter.class);
        // cantointhisinregistermoremultipleadaptertype
    }

    /**
     * registernew adaptertype
     * 
     * @param adapterType  adaptertypeidentifier
     * @param adapterClass adapterclass
     */
    public static void registerAdapter(String adapterType, Class<? extends KnowledgeBaseAdapter> adapterClass) {
        if (adapterRegistry.containsKey(adapterType)) {
            log.warn("adaptertype '{}' already exists，willisoverride", adapterType);
        }
        adapterRegistry.put(adapterType, adapterClass);
        log.info("registeradaptertype: {} -> {}", adapterType, adapterClass.getSimpleName());
    }

    /**
     * getadapterexample
     * 
     * @param adapterType adaptertype
     * @param config      configurationparameter
     * @return adapterexample
     */
    public static KnowledgeBaseAdapter getAdapter(String adapterType, Map<String, Object> config) {
        String cacheKey = buildCacheKey(adapterType, config);

        // checkcacheYesNoalready existsexample
        if (adapterCache.containsKey(cacheKey)) {
            log.debug("fromcachegetadapterexample: {}", cacheKey);
            return adapterCache.get(cacheKey);
        }

        // createnew adapterexample
        KnowledgeBaseAdapter adapter = createAdapter(adapterType, config);

        // cacheadapterexample (withcapacityamountlimitcheck)
        if (adapterCache.size() >= MAX_CACHE_SIZE) {
            log.warn("adaptercachealreadyreachuplimit ({})，executeinternalstoreprotectclear", MAX_CACHE_SIZE);
            // simpleprocess：directlyclearempty，productionenvironmentbelowsuggestionuse LRU
            adapterCache.clear();
        }

        adapterCache.put(cacheKey, adapter);
        log.info("createandcacheadapterexample: {}", cacheKey);

        return adapter;
    }

    /**
     * getadapterexample（noconfiguration）
     * 
     * @param adapterType adaptertype
     * @return adapterexample
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
     * checkadaptertypeYesNoalreadyregister
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
        log.info("clearadaptercache，commonclear {} example", cacheSize);
    }

    /**
     * removespecialadaptertype cache
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
        log.info("removeadaptertype '{}'  cache，commonremove {} example", adapterType, removedCount);
    }

    /**
     * getadapterfactorystatusinformation
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
     * createadapterexample
     * 
     * @param adapterType adaptertype
     * @param config      configurationparameter
     * @return adapterexample
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

            log.info("successcreateadapterexample: {}", adapterType);
            return adapter;

        } catch (Exception e) {
            log.error("createadapterexamplefailed: {}", adapterType, e);
            throw new RenException(ErrorCode.RAG_ADAPTER_CREATION_FAILED,
                    "createadapterfailed: " + adapterType + ", error: " + e.getMessage());
        }
    }

    /**
     * buildcachekey
     * 
     * @param adapterType adaptertype
     * @param config      configurationparameter
     * @return cachekey
     */
    private static String buildCacheKey(String adapterType, Map<String, Object> config) {
        if (config == null || config.isEmpty()) {
            return adapterType + "@default";
        }

        // basetoconfigurationparametergeneratecachekey
        StringBuilder keyBuilder = new StringBuilder(adapterType + "@");

        // useconfiguration hashvalueascachekey onepart
        int configHash = config.hashCode();
        keyBuilder.append(configHash);

        return keyBuilder.toString();
    }
}