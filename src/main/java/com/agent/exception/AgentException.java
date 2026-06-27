package com.agent.exception;

/**
 * Base exception for Agent-related errors
 */
public class AgentException extends Exception {
    
    public AgentException(String message) {
        super(message);
    }
    
    public AgentException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AgentException(Throwable cause) {
        super(cause);
    }
}
