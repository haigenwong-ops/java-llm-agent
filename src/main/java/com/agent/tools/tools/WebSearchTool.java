package com.agent.tools.tools;

import com.agent.tools.Tool;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web search tool (stub implementation)
 */
public class WebSearchTool implements Tool {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSearchTool.class);
    
    @Override
    public String getName() {
        return "web_search";
    }
    
    @Override
    public String getDescription() {
        return "Search the web for information on a given topic";
    }
    
    @Override
    public String getParametersSchema() {
        return "{\"query\": string}";
    }
    
    @Override
    public String execute(JsonObject input) throws Exception {
        if (!validateInput(input)) {
            throw new IllegalArgumentException("Invalid web search parameters");
        }
        
        String query = input.get("query").getAsString();
        logger.info("Web search query: {}", query);
        
        return String.format("Search results for '%s': [This is a stub implementation]", query);
    }
    
    @Override
    public boolean validateInput(JsonObject input) {
        try {
            return input.has("query") 
                && input.get("query").isJsonPrimitive()
                && !input.get("query").getAsString().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
