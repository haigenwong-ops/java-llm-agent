package com.agent.core;

/**
 * Configuration for Agent
 */
public class AgentConfig {
    
    private String llmBaseUrl;
    private String llmModel;
    private int maxRetries;
    private long retryDelayMs;
    private int maxMemoryMessages;
    private int contextTokenLimit;
    private boolean enableToolCalls;
    private long toolExecutionTimeoutMs;
    
    public static class Builder {
        private String llmBaseUrl = "http://localhost:11434";
        private String llmModel = "llama2";
        private int maxRetries = 3;
        private long retryDelayMs = 1000;
        private int maxMemoryMessages = 100;
        private int contextTokenLimit = 4000;
        private boolean enableToolCalls = true;
        private long toolExecutionTimeoutMs = 30000;
        
        public Builder llmBaseUrl(String url) {
            this.llmBaseUrl = url;
            return this;
        }
        
        public Builder llmModel(String model) {
            this.llmModel = model;
            return this;
            }
        
        public Builder maxRetries(int retries) {
            this.maxRetries = retries;
            return this;
        }
        
        public Builder retryDelayMs(long delayMs) {
            this.retryDelayMs = delayMs;
            return this;
        }
        
        public Builder maxMemoryMessages(int count) {
            this.maxMemoryMessages = count;
            return this;
        }
        
        public Builder contextTokenLimit(int tokens) {
            this.contextTokenLimit = tokens;
            return this;
        }
        
        public Builder enableToolCalls(boolean enable) {
            this.enableToolCalls = enable;
            return this;
        }
        
        public Builder toolExecutionTimeoutMs(long timeoutMs) {
            this.toolExecutionTimeoutMs = timeoutMs;
            return this;
        }
        
        public AgentConfig build() {
            AgentConfig config = new AgentConfig();
            config.llmBaseUrl = this.llmBaseUrl;
            config.llmModel = this.llmModel;
            config.maxRetries = this.maxRetries;
            config.retryDelayMs = this.retryDelayMs;
            config.maxMemoryMessages = this.maxMemoryMessages;
            config.contextTokenLimit = this.contextTokenLimit;
            config.enableToolCalls = this.enableToolCalls;
            config.toolExecutionTimeoutMs = this.toolExecutionTimeoutMs;
            return config;
        }
    }
    
    private AgentConfig() {}
    
    public String getLlmBaseUrl() { return llmBaseUrl; }
    public String getLlmModel() { return llmModel; }
    public int getMaxRetries() { return maxRetries; }
    public long getRetryDelayMs() { return retryDelayMs; }
    public int getMaxMemoryMessages() { return maxMemoryMessages; }
    public int getContextTokenLimit() { return contextTokenLimit; }
    public boolean isEnableToolCalls() { return enableToolCalls; }
    public long getToolExecutionTimeoutMs() { return toolExecutionTimeoutMs; }
    
    @Override
    public String toString() {
        return "AgentConfig{" +
                "llmBaseUrl='" + llmBaseUrl + '\'' +
                ", llmModel='" + llmModel + '\'' +
                ", maxRetries=" + maxRetries +
                ", contextTokenLimit=" + contextTokenLimit +
                ", enableToolCalls=" + enableToolCalls +
                '}';
    }
}
