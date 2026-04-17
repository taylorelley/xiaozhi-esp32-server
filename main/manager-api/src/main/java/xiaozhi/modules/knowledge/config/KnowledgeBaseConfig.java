package xiaozhi.modules.knowledge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xiaozhi.modules.knowledge.rag.KnowledgeBaseAdapterFactory;

/**
 * Knowledge baseconfigurationclass
 * configurationKnowledge baserelated Bean
 */
@Configuration
public class KnowledgeBaseConfig {

    /**
     * 提供KnowledgeBaseAdapterFactory Bean实example
     * @return KnowledgeBaseAdapterFactory实example
     */
    @Bean
    public KnowledgeBaseAdapterFactory knowledgeBaseAdapterFactory() {
        return new KnowledgeBaseAdapterFactory();
    }
}