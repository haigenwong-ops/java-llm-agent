package com.agent.memory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single message in the conversation
 */
public class Message {
    
    public enum Role {
        USER("user"),
        ASSISTANT("assistant"),
        SYSTEM("system");
        
        private final String value;
        
        Role(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    private Role role;
    private String content;
    private LocalDateTime timestamp;
    private String metadata;
    
    public Message(Role role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
    
    public Message(Role role, String content, String metadata) {
        this(role, content);
        this.metadata = metadata;
    }
    
    public Role getRole() {
        return role;
    }
    
    public String getContent() {
        return content;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return String.format("[%s] %s: %s", 
            timestamp.format(formatter), role.getValue(), content);
    }
}
