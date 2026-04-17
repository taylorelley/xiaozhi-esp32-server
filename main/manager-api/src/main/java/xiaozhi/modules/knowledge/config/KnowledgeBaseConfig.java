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
     * 提供KnowledgeBaseAdapterFactory Beanexample
     * @return KnowledgeBaseAdapterFactoryexample
     */
    @Bean
    public KnowledgeBaseAdapterFactory knowledgeBaseAdapterFactory() {
        return new KnowledgeBaseAdapterFactory();
    }
}