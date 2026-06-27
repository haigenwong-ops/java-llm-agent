package com.agent.llm;

import com.agent.exception.AgentException;
import com.agent.llm.providers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating LLM client instances based on provider type
 */
public class LLMClientFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(LLMClientFactory.class);
    
    public enum Provider {
        OLLAMA("ollama"),
        LM_STUDIO("lm_studio"),
        LOCAL_AI("localai"),
        TEXT_GEN_WEBUI("text_gen_webui");
        
        private final String value;
        
        Provider(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static Provider fromString(String value) {
            for (Provider provider : Provider.values()) {
                if (provider.value.equalsIgnoreCase(value)) {
                    return provider;
                }
            }
            throw new IllegalArgumentException("Unknown provider: " + value);
        }
    }
    
    /**
     * Create LLM client based on provider type
     */
    public static LLMClient createClient(Provider provider, String baseUrl, String model) 
            throws AgentException {
        logger.info("Creating LLM client for provider: {}", provider.getValue());
        
        switch (provider) {
            case OLLAMA:
                return new OllamaClient(baseUrl, model);
            case LM_STUDIO:
                return new LMStudioClient(baseUrl, model);
            case LOCAL_AI:
                return new LocalAIClient(baseUrl, model);
            case TEXT_GEN_WEBUI:
                return new TextGenWebUIClient(baseUrl, model);
            default:
                throw new AgentException("Unsupported provider: " + provider);
        }
    }
    
    /**
     * Create LLM client by string provider name
     */
    public static LLMClient createClient(String providerName, String baseUrl, String model) 
            throws AgentException {
        try {
            Provider provider = Provider.fromString(providerName);
            return createClient(provider, baseUrl, model);
        } catch (IllegalArgumentException e) {
            throw new AgentException("Invalid provider name: " + providerName, e);
        }
    }
    
    /**
     * Get default base URL for provider
     */
    public static String getDefaultBaseUrl(Provider provider) {
        switch (provider) {
            case OLLAMA:
                return "http://localhost:11434";
            case LM_STUDIO:
                return "http://localhost:1234";
            case LOCAL_AI:
                return "http://localhost:8080";
            case TEXT_GEN_WEBUI:
                return "http://localhost:5000";
            default:
                return "http://localhost:8000";
        }
    }
    
    /**
     * Get supported providers
     */
    public static Provider[] getSupportedProviders() {
        return Provider.values();
    }
    
    /**
     * Get provider names as string array
     */
    public static String[] getProviderNames() {
        Provider[] providers = getSupportedProviders();
        String[] names = new String[providers.length];
        for (int i = 0; i < providers.length; i++) {
            names[i] = providers[i].getValue();
        }
        return names;
    }
}
