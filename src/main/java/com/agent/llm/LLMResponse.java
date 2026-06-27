package com.agent.llm;

/**
 * Response from LLM
 */
public class LLMResponse {
    
    private String content;
    private boolean toolCall;
    private String toolName;
    private String toolInput;
    private int inputTokens;
    private int outputTokens;
    private String model;
    
    public LLMResponse(String content) {
        this.content = content;
        this.toolCall = false;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public boolean isToolCall() {
        return toolCall;
    }
    
    public void setToolCall(boolean toolCall) {
        this.toolCall = toolCall;
    }
    
    public String getToolName() {
        return toolName;
    }
    
    public void setToolName(String toolName) {
        this.toolName = toolName;
    }
    
    public String getToolInput() {
        return toolInput;
    }
    
    public void setToolInput(String toolInput) {
        this.toolInput = toolInput;
    }
    
    public int getInputTokens() {
        return inputTokens;
    }
    
    public void setInputTokens(int inputTokens) {
        this.inputTokens = inputTokens;
    }
    
    public int getOutputTokens() {
        return outputTokens;
    }
    
    public void setOutputTokens(int outputTokens) {
        this.outputTokens = outputTokens;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    @Override
    public String toString() {
        return "LLMResponse{" +
                "content='" + content + '\'' +
                ", toolCall=" + toolCall +
                ", toolName='" + toolName + '\'' +
                ", model='" + model + '\'' +
                '}';
    }
}
