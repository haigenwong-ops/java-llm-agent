package com.agent.tools.tools;

import com.agent.tools.Tool;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple code execution tool
 */
public class CodeExecutionTool implements Tool {
    
    private static final Logger logger = LoggerFactory.getLogger(CodeExecutionTool.class);
    
    @Override
    public String getName() {
        return "execute_code";
    }
    
    @Override
    public String getDescription() {
        return "Execute simple Java code snippets and return the result";
    }
    
    @Override
    public String getParametersSchema() {
        return "{\"language\": \"java\", \"code\": string}";
    }
    
    @Override
    public String execute(JsonObject input) throws Exception {
        if (!validateInput(input)) {
            throw new IllegalArgumentException("Invalid code execution parameters");
        }
        
        String language = input.get("language").getAsString();
        String code = input.get("code").getAsString();
        
        logger.info("Executing {} code", language);
        
        if (code.contains("System.") || code.contains("Runtime.") || code.contains("File")) {
            throw new SecurityException("Dangerous operations not allowed");
        }
        
        try {
            Object result = evaluateExpression(code);
            return "Code execution result: " + result;
        } catch (Exception e) {
            throw new Exception("Execution error: " + e.getMessage());
        }
    }
    
    private Object evaluateExpression(String expression) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(expression);
        return evaluator.parse();
    }
    
    @Override
    public boolean validateInput(JsonObject input) {
        try {
            return input.has("language") 
                && input.has("code")
                && input.get("language").isJsonPrimitive()
                && input.get("code").isJsonPrimitive();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Simple expression evaluator for math operations
     */
    private static class ExpressionEvaluator {
        private int pos = -1, ch;
        private String str;
        
        ExpressionEvaluator(String expression) {
            this.str = expression;
        }
        
        private void nextChar() {
            ch = (++pos < str.length()) ? str.charAt(pos) : -1;
        }
        
        private boolean eat(int charToEat) {
            while (ch == ' ') nextChar();
            if (ch == charToEat) {
                nextChar();
                return true;
            }
            return false;
        }
        
        Object parse() {
            nextChar();
            double x = parseExpression();
            if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
            return x;
        }
        
        private double parseExpression() {
            double x = parseTerm();
            while (true) {
                if (eat('+')) x += parseTerm();
                else if (eat('-')) x -= parseTerm();
                else return x;
            }
        }
        
        private double parseTerm() {
            double x = parseFactor();
            while (true) {
                if (eat('*')) x *= parseFactor();
                else if (eat('/')) x /= parseFactor();
                else return x;
            }
        }
        
        private double parseFactor() {
            if (eat('+')) return parseFactor();
            if (eat('-')) return -parseFactor();
            
            double x;
            int startPos = this.pos;
            if (eat('(')) {
                x = parseExpression();
                eat(')');
            } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                x = Double.parseDouble(str.substring(startPos, this.pos));
            } else {
                throw new RuntimeException("Unexpected: " + (char)ch);
            }
            
            return x;
        }
    }
}
