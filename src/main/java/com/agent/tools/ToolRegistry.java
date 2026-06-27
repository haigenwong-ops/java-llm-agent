package com.agent.tools;

import java.util.*;

/**
 * Registry for managing available tools
 */
public class ToolRegistry {
    
    private final Map<String, Tool> tools;
    
    public ToolRegistry() {
        this.tools = new HashMap<>();
    }
    
    public void register(Tool tool) {
        if (tool == null || tool.getName() == null) {
            throw new IllegalArgumentException("Tool and tool name cannot be null");
        }
        tools.put(tool.getName(), tool);
    }
    
    public Tool getTool(String name) {
        return tools.get(name);
    }
    
    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }
    
    public Collection<Tool> getAllTools() {
        return new ArrayList<>(tools.values());
    }
    
    public Set<String> getToolNames() {
        return new HashSet<>(tools.keySet());
    }
    
    public String getToolsDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Available tools:\n");
        for (Tool tool : tools.values()) {
            sb.append("- ").append(tool.getName()).append(": ").append(tool.getDescription()).append("\n");
            sb.append("  Schema: ").append(tool.getParametersSchema()).append("\n");
        }
        return sb.toString();
    }
    
    public int getToolCount() {
        return tools.size();
    }
    
    public void clear() {
        tools.clear();
    }
}
