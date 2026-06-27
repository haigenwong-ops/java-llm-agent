package com.agent.tools;

import com.google.gson.JsonObject;

/**
 * Interface for tools that the agent can use
 */
public interface Tool {
    
    /**
     * Get tool name
     */
    String getName();
    
    /**
     * Get tool description
     */
    String getDescription();
    
    /**
     * Get tool parameters schema (for LLM to understand)
     */
    String getParametersSchema();
    
    /**
     * Execute the tool with given input
     */
    String execute(JsonObject input) throws Exception;
    
    /**
     * Validate input parameters
     */
    boolean validateInput(JsonObject input);
    
    /**
     * Get human-readable tool usage example
     */
    default String getExample() {
        return getName() + ": " + getDescription();
    }
}
