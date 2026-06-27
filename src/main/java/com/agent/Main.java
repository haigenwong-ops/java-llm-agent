package com.agent;

import com.agent.core.Agent;
import com.agent.core.AgentConfig;
import com.agent.core.AgentResponse;
import com.agent.tools.tools.CalculatorTool;
import com.agent.tools.tools.CodeExecutionTool;
import com.agent.tools.tools.WebSearchTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Main entry point for the Java LLM Agent
 */
public class Main {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        try {
            logger.info("========================================");
            logger.info("Java LLM Agent - Started");
            logger.info("========================================");
            
            // Configure agent
            AgentConfig config = new AgentConfig.Builder()
                .llmBaseUrl("http://localhost:11434")
                .llmModel("llama2")  // Change to your model (llama3, phi3, etc)
                .maxRetries(3)
                .retryDelayMs(1000)
                .maxMemoryMessages(50)
                .contextTokenLimit(4000)
                .enableToolCalls(true)
                .build();
            
            // Create agent
            Agent agent = new Agent(config);
            
            // Register tools
            agent.registerTool(new CalculatorTool());
            agent.registerTool(new WebSearchTool());
            agent.registerTool(new CodeExecutionTool());
            
            logger.info("Agent initialized with {} tools", agent.getToolRegistry().getToolCount());
            
            // Interactive chat loop
            runInteractiveMode(agent);
            
        } catch (Exception e) {
            logger.error("Fatal error", e);
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void runInteractiveMode(Agent agent) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   Welcome to Java LLM Agent             ║");
        System.out.println("║   Commands: 'exit', 'history', 'clear'  ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        while (agent.isRunning()) {
            try {
                System.out.print("You: ");
                String userInput = scanner.nextLine().trim();
                
                if (userInput.isEmpty()) {
                    continue;
                }
                
                if (userInput.equalsIgnoreCase("exit")) {
                    agent.shutdown();
                    System.out.println("Agent stopped. Goodbye!");
                    break;
                }
                
                if (userInput.equalsIgnoreCase("history")) {
                    System.out.println(agent.getConversationHistory());
                    continue;
                }
                
                if (userInput.equalsIgnoreCase("clear")) {
                    agent.clearHistory();
                    System.out.println("Conversation history cleared.\n");
                    continue;
                }
                
                // Process input
                AgentResponse response = agent.processInput(userInput);
                
                if (response.isSuccess()) {
                    System.out.println("\nAgent: " + response.getMessage() + "\n");
                } else if (response.isToolCall()) {
                    System.out.println("\nAgent [Tool Call]: " + response.getToolName());
                    System.out.println("Result: " + response.getMessage() + "\n");
                } else if (response.isError()) {
                    System.out.println("\nAgent [Error]: " + response.getMessage() + "\n");
                }
                
            } catch (Exception e) {
                logger.error("Error in interactive mode", e);
                System.out.println("Error: " + e.getMessage());
            }
        }
        
        scanner.close();
    }
}
