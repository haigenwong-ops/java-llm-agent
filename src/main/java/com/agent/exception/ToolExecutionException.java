package com.agent.exception;

/**
 * Exception for tool execution failures
 */
public class ToolExecutionException extends AgentException {
    
    private String toolName;
    private String toolInput;
    
    public ToolExecutionException(String toolName, String message) {
        super("Tool '" + toolName + "' execution failed: " + message);
        this.toolName = toolName;
    }
    
    public ToolExecutionException(String toolName, String message, Throwable cause) {
        super("Tool '" + toolName + "' execution failed: " + message, cause);
        this.toolName = toolName;
    }
    
    public String getToolName() {
        return toolName;
    }
    
    public void setToolInput(String toolInput) {
        this.toolInput = toolInput;
    }
    
    public String getToolInput() {
        return toolInput;
    }
}
