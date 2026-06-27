package com.agent.llm;

import com.google.gson.*;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ollama LLM client implementation
 */
public class OllamaClient implements LLMClient {
    
    private static final Logger logger = LoggerFactory.getLogger(OllamaClient.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    private final String baseUrl;
    private final OkHttpClient httpClient;
    private String currentModel;
    private int temperature;
    private int topK;
    private double topP;
    
    public OllamaClient(String baseUrl, String defaultModel) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build();
        this.currentModel = defaultModel;
        this.temperature = 0;
        this.topK = 40;
        this.topP = 0.9;
    }
    
    @Override
    public LLMResponse chat(String userMessage) throws Exception {
        List<String> messages = new ArrayList<>();
        messages.add(userMessage);
        return chat(messages);
    }
    
    @Override
    public LLMResponse chat(List<String> messages) throws Exception {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("Messages cannot be empty");
        }
        
        String prompt = String.join("\n", messages);
        return callOllama(prompt);
    }
    
    private LLMResponse callOllama(String prompt) throws Exception {
        String url = baseUrl + "/api/generate";
        
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", currentModel);
        requestBody.addProperty("prompt", prompt);
        requestBody.addProperty("stream", false);
        requestBody.addProperty("temperature", temperature);
        requestBody.addProperty("top_k", topK);
        requestBody.addProperty("top_p", topP);
        
        logger.info("Calling Ollama with model: {}", currentModel);
        
        Request request = new Request.Builder()
            .url(url)
            .post(RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json")))
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new Exception("Ollama API error: " + response.code() + " - " + errorBody);
            }
            
            String responseBody = response.body().string();
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            
            LLMResponse llmResponse = new LLMResponse(jsonResponse.get("response").getAsString());
            llmResponse.setModel(currentModel);
            
            return llmResponse;
        }
    }
    
    @Override
    public boolean testConnection() throws Exception {
        try {
            String url = baseUrl + "/api/tags";
            Request request = new Request.Builder()
                .url(url)
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            logger.error("Connection test failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public List<String> listModels() throws Exception {
        List<String> models = new ArrayList<>();
        
        try {
            String url = baseUrl + "/api/tags";
            Request request = new Request.Builder()
                .url(url)
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
                    JsonArray modelsArray = json.getAsJsonArray("models");
                    if (modelsArray != null) {
                        for (JsonElement element : modelsArray) {
                            models.add(element.getAsJsonObject().get("name").getAsString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to list models: {}", e.getMessage());
        }
        
        return models;
    }
    
    @Override
    public void setModel(String modelName) {
        this.currentModel = modelName;
    }
    
    @Override
    public String getModel() {
        return currentModel;
    }
    
    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }
    
    public void setTopK(int topK) {
        this.topK = topK;
    }
    
    public void setTopP(double topP) {
        this.topP = topP;
    }
}
