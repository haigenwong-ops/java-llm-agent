package com.agent.core;

/**
 * Response from Agent
 */
public class AgentResponse {
    
    public enum Status {
        SUCCESS,
        TOOL_CALL,
        ERROR,
        PENDING
    }
    
    private Status status;
    private String message;
    private String toolName;
    private String toolInput;
    private int retries;
    private long executionTimeMs;
    private Throwable error;
    
    public AgentResponse(Status status, String message) {
        this.status = status;
        this.message = message;
        this.retries = 0;
        this.executionTimeMs = 0;
    }
    
    public AgentResponse(Status status, String message, String toolName, String toolInput) {
        this(status, message);
        this.toolName = toolName;
        this.toolInput = toolInput;
    }
    
    public AgentResponse(Status status, String message, Throwable error) {
        this(status, message);
        this.error = error;
    }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    public String getToolInput() { return toolInput; }
    public void setToolInput(String toolInput) { this.toolInput = toolInput; }
    public int getRetries() { return retries; }
    public void setRetries(int retries) { this.retries = retries; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    public Throwable getError() { return error; }
    public void setError(Throwable error) { this.error = error; }
    
    public boolean isSuccess() { return status == Status.SUCCESS; }
    public boolean isToolCall() { return status == Status.TOOL_CALL; }
    public boolean isError() { return status == Status.ERROR; }
    
    @Override
    public String toString() {
        return "AgentResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", toolName='" + toolName + '\'' +
                ", executionTimeMs=" + executionTimeMs +
                '}';
    }
}
