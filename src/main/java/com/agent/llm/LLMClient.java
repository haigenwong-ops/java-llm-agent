package com.agent.llm;

import java.util.List;

/**
 * Interface for LLM clients
 */
public interface LLMClient {
    
    /**
     * Send a message to the LLM and get response
     */
    LLMResponse chat(String userMessage) throws Exception;
    
    /**
     * Send multiple messages (conversation context) to LLM
     */
    LLMResponse chat(List<String> messages) throws Exception;
    
    /**
     * Test connection to LLM
     */
    boolean testConnection() throws Exception;
    
    /**
     * Get available models
     */
    List<String> listModels() throws Exception;
    
    /**
     * Set current model
     */
    void setModel(String modelName);
    
    /**
     * Get current model
     */
    String getModel();
}
