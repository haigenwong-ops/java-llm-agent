package com.agent.llm.providers;

import com.agent.llm.LLMClient;
import com.agent.llm.LLMResponse;
import com.google.gson.*;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * LM Studio Local API client
 * LM Studio is a user-friendly desktop app for running LLMs locally
 * https://lmstudio.ai/
 */
public class LMStudioClient implements LLMClient {
    
    private static final Logger logger = LoggerFactory.getLogger(LMStudioClient.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    private final String baseUrl;
    private final OkHttpClient httpClient;
    private String currentModel;
    private double temperature;
    private int maxTokens;
    private double topP;
    
    public LMStudioClient(String baseUrl, String defaultModel) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build();
        this.currentModel = defaultModel;
        this.temperature = 0.7;
        this.maxTokens = 2048;
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
        return callLMStudio(prompt);
    }
    
    private LLMResponse callLMStudio(String prompt) throws Exception {
        String url = baseUrl + "/v1/completions";
        
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", currentModel);
        requestBody.addProperty("prompt", prompt);
        requestBody.addProperty("temperature", temperature);
        requestBody.addProperty("max_tokens", maxTokens);
        requestBody.addProperty("top_p", topP);
        requestBody.addProperty("stream", false);
        
        logger.info("Calling LM Studio with model: {}", currentModel);
        
        Request request = new Request.Builder()
            .url(url)
            .post(RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json")))
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new Exception("LM Studio API error: " + response.code() + " - " + errorBody);
            }
            
            String responseBody = response.body().string();
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            
            String content = jsonResponse.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .get("text").getAsString();
            
            LLMResponse llmResponse = new LLMResponse(content);
            llmResponse.setModel(currentModel);
            llmResponse.setProviderName(getProviderName());
            
            return llmResponse;
        }
    }
    
    @Override
    public boolean testConnection() throws Exception {
        try {
            String url = baseUrl + "/v1/models";
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
            String url = baseUrl + "/v1/models";
            Request request = new Request.Builder()
                .url(url)
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
                    JsonArray modelsArray = json.getAsJsonArray("data");
                    if (modelsArray != null) {
                        for (JsonElement element : modelsArray) {
                            models.add(element.getAsJsonObject().get("id").getAsString());
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
    
    @Override
    public String getProviderName() {
        return "LM Studio";
    }
    
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    
    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    public void setTopP(double topP) {
        this.topP = topP;
    }
}
