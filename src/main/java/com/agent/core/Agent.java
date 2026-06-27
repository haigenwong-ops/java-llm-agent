package com.agent.core;

import com.agent.exception.AgentException;
import com.agent.exception.ToolExecutionException;
import com.agent.llm.LLMClient;
import com.agent.llm.LLMResponse;
import com.agent.llm.OllamaClient;
import com.agent.memory.ConversationMemory;
import com.agent.memory.Memory;
import com.agent.memory.Message;
import com.agent.tools.Tool;
import com.agent.tools.ToolRegistry;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Agent class that orchestrates LLM calls, memory management, and tool execution
 */
public class Agent {
    
    private static final Logger logger = LoggerFactory.getLogger(Agent.class);
    private static final Gson gson = new Gson();
    
    private final LLMClient llmClient;
    private final Memory memory;
    private final ToolRegistry toolRegistry;
    private final AgentConfig config;
    private volatile boolean isRunning;
    
    public Agent(AgentConfig config) throws AgentException {
        this.config = config;
        this.llmClient = new OllamaClient(config.getLlmBaseUrl(), config.getLlmModel());
        this.memory = new ConversationMemory(config.getMaxMemoryMessages());
        this.toolRegistry = new ToolRegistry();
        this.isRunning = false;
        initialize();
    }
    
    private void initialize() throws AgentException {
        try {
            logger.info("Initializing Agent with config: {}", config);
            if (!llmClient.testConnection()) {
                throw new AgentException("Failed to connect to LLM at " + config.getLlmBaseUrl());
            }
            isRunning = true;
            logger.info("Agent initialized successfully");
        } catch (Exception e) {
            throw new AgentException("Agent initialization failed", e);
        }
    }
    
    public void registerTool(Tool tool) {
        toolRegistry.register(tool);
        logger.info("Tool registered: {}", tool.getName());
    }
    
    public AgentResponse processInput(String userInput) throws AgentException {
        if (!isRunning) {
            throw new AgentException("Agent is not running");
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            memory.addMessage(new Message(Message.Role.USER, userInput));
            logger.info("Processing user input: {}", userInput);
            
            LLMResponse llmResponse = callLLMWithRetry(userInput);
            
            if (llmResponse.isToolCall() && config.isEnableToolCalls()) {
                return handleToolCall(llmResponse, startTime);
            }
            
            memory.addMessage(new Message(Message.Role.ASSISTANT, llmResponse.getContent()));
            
            AgentResponse response = new AgentResponse(
                AgentResponse.Status.SUCCESS,
                llmResponse.getContent()
            );
            response.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            logger.info("Agent response generated in {}ms", response.getExecutionTimeMs());
            return response;
            
        } catch (Exception e) {
            logger.error("Error processing input", e);
            AgentResponse errorResponse = new AgentResponse(
                AgentResponse.Status.ERROR,
                "Error: " + e.getMessage(),
                e
            );
            errorResponse.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            return errorResponse;
        }
    }
    
    private LLMResponse callLLMWithRetry(String userInput) throws Exception {
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < config.getMaxRetries()) {
            try {
                String context = buildPromptWithContext(userInput);
                return llmClient.chat(context);
            } catch (Exception e) {
                lastException = e;
                attempt++;
                
                if (attempt < config.getMaxRetries()) {
                    logger.warn("LLM call failed (attempt {}/{}), retrying...", 
                        attempt, config.getMaxRetries());
                    Thread.sleep(config.getRetryDelayMs());
                } else {
                    logger.error("LLM call failed after {} attempts", config.getMaxRetries());
                }
            }
        }
        
        throw new AgentException("LLM call failed after " + config.getMaxRetries() + " retries", 
            lastException);
    }
    
    private String buildPromptWithContext(String userInput) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a helpful AI assistant.\n");
        
        if (config.isEnableToolCalls() && toolRegistry.getToolCount() > 0) {
            prompt.append("\n").append(toolRegistry.getToolsDescription());
            prompt.append("\nIf you need to use a tool, respond with: [TOOL_CALL]toolname(params)[/TOOL_CALL]\n");
        }
        
        String contextWindow = memory.getContextWindow(config.getContextTokenLimit());
        if (!contextWindow.isEmpty()) {
            prompt.append("\nConversation history:\n").append(contextWindow);
        }
        
        prompt.append("\nUser: ").append(userInput);
        prompt.append("\nAssistant: ");
        
        return prompt.toString();
    }
    
    private AgentResponse handleToolCall(LLMResponse llmResponse, long startTime) 
            throws AgentException {
        try {
            String toolName = llmResponse.getToolName();
            String toolInput = llmResponse.getToolInput();
            
            logger.info("Tool call detected: {} with input: {}", toolName, toolInput);
            
            if (!toolRegistry.hasTool(toolName)) {
                throw new ToolExecutionException(toolName, "Tool not found in registry");
            }
            
            Tool tool = toolRegistry.getTool(toolName);
            JsonObject inputJson = gson.fromJson(toolInput, JsonObject.class);
            
            if (!tool.validateInput(inputJson)) {
                throw new ToolExecutionException(toolName, "Input validation failed");
            }
            
            String toolResult = tool.execute(inputJson);
            
            memory.addMessage(new Message(
                Message.Role.ASSISTANT,
                "Calling tool: " + toolName,
                "Tool call: " + toolName
            ));
            memory.addMessage(new Message(
                Message.Role.USER,
                toolResult,
                "Tool result: " + toolName
            ));
            
            LLMResponse followUpResponse = callLLMWithRetry(
                "Tool '" + toolName + "' returned: " + toolResult + 
                "\nBased on this result, provide a helpful response to the user."
            );
            
            memory.addMessage(new Message(Message.Role.ASSISTANT, followUpResponse.getContent()));
            
            AgentResponse response = new AgentResponse(
                AgentResponse.Status.SUCCESS,
                followUpResponse.getContent(),
                toolName,
                toolInput
            );
            response.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            
            return response;
            
        } catch (ToolExecutionException e) {
            logger.error("Tool execution failed: {}", e.getMessage());
            throw new AgentException("Tool execution failed", e);
        }
    }
    
    public Memory getMemory() {
        return memory;
    }
    
    public ToolRegistry getToolRegistry() {
        return toolRegistry;
    }
    
    public void clearHistory() {
        memory.clear();
        logger.info("Conversation history cleared");
    }
    
    public void shutdown() {
        isRunning = false;
        logger.info("Agent shutdown");
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public String getConversationHistory() {
        return memory.toString();
    }
}
