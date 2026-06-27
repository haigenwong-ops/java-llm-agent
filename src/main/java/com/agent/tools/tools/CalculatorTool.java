package com.agent.tools.tools;

import com.agent.tools.Tool;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple calculator tool for basic math operations
 */
public class CalculatorTool implements Tool {
    
    private static final Logger logger = LoggerFactory.getLogger(CalculatorTool.class);
    
    @Override
    public String getName() {
        return "calculator";
    }
    
    @Override
    public String getDescription() {
        return "Performs basic mathematical calculations. Supports: add, subtract, multiply, divide";
    }
    
    @Override
    public String getParametersSchema() {
        return "{\"operation\": \"add|subtract|multiply|divide\", \"a\": number, \"b\": number}";
    }
    
    @Override
    public String execute(JsonObject input) throws Exception {
        if (!validateInput(input)) {
            throw new IllegalArgumentException("Invalid calculator parameters");
        }
        
        String operation = input.get("operation").getAsString();
        double a = input.get("a").getAsDouble();
        double b = input.get("b").getAsDouble();
        
        double result;
        switch (operation.toLowerCase()) {
            case "add":
                result = a + b;
                break;
            case "subtract":
                result = a - b;
                break;
            case "multiply":
                result = a * b;
                break;
            case "divide":
                if (b == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                result = a / b;
                break;
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
        
        logger.info("Calculator: {} {} {} = {}", a, operation, b, result);
        return String.format("Result of %s operation: %.2f", operation, result);
    }
    
    @Override
    public boolean validateInput(JsonObject input) {
        try {
            return input.has("operation") 
                && input.has("a") 
                && input.has("b")
                && input.get("operation").isJsonPrimitive()
                && input.get("a").isJsonPrimitive()
                && input.get("b").isJsonPrimitive();
        } catch (Exception e) {
            return false;
        }
    }
}
