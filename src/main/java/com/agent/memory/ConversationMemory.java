package com.agent.memory;

import java.util.*;

/**
 * Conversation memory implementation with context management
 */
public class ConversationMemory implements Memory {
    
    private final List<Message> messages;
    private final int maxMessages;
    private static final int DEFAULT_MAX_MESSAGES = 100;
    private static final int TOKENS_PER_WORD = 1;
    
    public ConversationMemory() {
        this(DEFAULT_MAX_MESSAGES);
    }
    
    public ConversationMemory(int maxMessages) {
        this.messages = Collections.synchronizedList(new ArrayList<>());
        this.maxMessages = maxMessages;
    }
    
    @Override
    public void addMessage(Message message) {
        messages.add(message);
        if (messages.size() > maxMessages) {
            messages.remove(0);
        }
    }
    
    @Override
    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }
    
    @Override
    public List<Message> getRecentMessages(int count) {
        int startIndex = Math.max(0, messages.size() - count);
        return new ArrayList<>(messages.subList(startIndex, messages.size()));
    }
    
    @Override
    public void clear() {
        messages.clear();
    }
    
    @Override
    public int getMessageCount() {
        return messages.size();
    }
    
    @Override
    public String getContextWindow() {
        return getContextWindow(4000);
    }
    
    @Override
    public String getContextWindow(int maxTokens) {
        StringBuilder context = new StringBuilder();
        int tokenCount = 0;
        List<Message> messagesToInclude = new ArrayList<>();
        
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message msg = messages.get(i);
            int msgTokens = estimateTokens(msg.getContent());
            
            if (tokenCount + msgTokens <= maxTokens) {
                messagesToInclude.add(0, msg);
                tokenCount += msgTokens;
            } else {
                break;
            }
        }
        
        for (Message message : messagesToInclude) {
            context.append(String.format("%s: %s\n", 
                message.getRole().getValue(), 
                message.getContent()));
        }
        
        return context.toString();
    }
    
    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return Math.max(1, text.split("\\s+").length);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Conversation Memory (").append(messages.size()).append(" messages) ===\n");
        for (Message msg : messages) {
            sb.append(msg).append("\n");
        }
        return sb.toString();
    }
}
