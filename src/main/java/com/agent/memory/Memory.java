package com.agent.memory;

import java.util.List;

/**
 * Interface for memory management
 */
public interface Memory {
    
    /**
     * Add a message to memory
     */
    void addMessage(Message message);
    
    /**
     * Get all messages
     */
    List<Message> getMessages();
    
    /**
     * Get recent messages (last N)
     */
    List<Message> getRecentMessages(int count);
    
    /**
     * Clear all messages
     */
    void clear();
    
    /**
     * Get message count
     */
    int getMessageCount();
    
    /**
     * Get context window (formatted messages for LLM)
     */
    String getContextWindow();
    
    /**
     * Get context window with max tokens limit
     */
    String getContextWindow(int maxTokens);
}
