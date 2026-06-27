package com.agent.tools.tools;

import com.agent.tools.Tool;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Weather tool that fetches real-time weather data from Open-Meteo API (free, no API key required)
 */
public class WeatherTool implements Tool {
    
    private static final Logger logger = LoggerFactory.getLogger(WeatherTool.class);
    private static final String BASE_URL = "https://geocoding-api.open-meteo.com/v1/search";
    private static final String WEATHER_URL = "https://api.open-meteo.com/v1/forecast";
    private final OkHttpClient httpClient;
    
    public WeatherTool() {
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    }
    
    @Override
    public String getName() {
        return "weather";
    }
    
    @Override
    public String getDescription() {
        return "Get current weather and forecast for a city. Requires: city name";
    }
    
    @Override
    public String getParametersSchema() {
        return "{\"city\": string, \"days\": number (optional, 0-16, default: 0 for current only)}";
    }
    
    @Override
    public String execute(JsonObject input) throws Exception {
        if (!validateInput(input)) {
            throw new IllegalArgumentException("Invalid weather parameters");
        }
        
        String city = input.get("city").getAsString();
        int days = input.has("days") ? input.get("days").getAsInt() : 0;
        
        if (days < 0 || days > 16) {
            days = 0;
        }
        
        logger.info("Fetching weather for city: {} with {} days forecast", city, days);
        
        try {
            // First, get coordinates from city name
            JsonObject coordinates = geocodeCity(city);
            if (coordinates == null) {
                return String.format("Sorry, I couldn't find the city '%s'. Please try again.", city);
            }
            
            double latitude = coordinates.get("latitude").getAsDouble();
            double longitude = coordinates.get("longitude").getAsDouble();
            String cityName = coordinates.get("name").getAsString();
            String country = coordinates.has("country") ? coordinates.get("country").getAsString() : "Unknown";
            
            // Get weather data
            return getWeatherData(cityName, country, latitude, longitude, days);
            
        } catch (Exception e) {
            logger.error("Error fetching weather", e);
            return "Error fetching weather data: " + e.getMessage();
        }
    }
    
    /**
     * Geocode city name to get coordinates
     */
    private JsonObject geocodeCity(String city) throws Exception {
        String url = BASE_URL + "?name=" + city + "&count=1&language=en&format=json";
        
        Request request = new Request.Builder()
            .url(url)
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }
            
            JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
            
            if (!json.has("results") || json.getAsJsonArray("results").size() == 0) {
                return null;
            }
            
            return json.getAsJsonArray("results").get(0).getAsJsonObject();
        }
    }
    
    /**
     * Get weather data for coordinates
     */
    private String getWeatherData(String city, String country, double latitude, double longitude, int forecastDays) 
            throws Exception {
        
        StringBuilder urlBuilder = new StringBuilder(WEATHER_URL)
            .append("?latitude=").append(latitude)
            .append("&longitude=").append(longitude)
            .append("&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m")
            .append("&temperature_unit=celsius");
        
        if (forecastDays > 0) {
            urlBuilder.append("&forecast_days=").append(forecastDays)
                .append("&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum,wind_speed_10m_max");
        }
        
        Request request = new Request.Builder()
            .url(urlBuilder.toString())
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return "Failed to fetch weather data";
            }
            
            JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
            
            return formatWeatherResponse(city, country, json, forecastDays);
        }
    }
    
    /**
     * Format weather response in readable format
     */
    private String formatWeatherResponse(String city, String country, JsonObject weatherData, int forecastDays) {
        StringBuilder result = new StringBuilder();
        
        result.append("🌍 Weather in ").append(city).append(", ").append(country).append("\n\n");
        
        // Current weather
        if (weatherData.has("current")) {
            JsonObject current = weatherData.getAsJsonObject("current");
            
            result.append("📊 Current Conditions:\n");
            result.append("  Temperature: ").append(String.format("%.1f°C", current.get("temperature_2m").getAsDouble())).append("\n");
            result.append("  Feels Like: ").append(String.format("%.1f°C", current.get("apparent_temperature").getAsDouble())).append("\n");
            result.append("  Humidity: ").append(current.get("relative_humidity_2m").getAsInt()).append("%\n");
            result.append("  Wind Speed: ").append(String.format("%.1f", current.get("wind_speed_10m").getAsDouble())).append(" km/h\n");
            result.append("  Weather: ").append(getWeatherDescription(current.get("weather_code").getAsInt())).append("\n");
        }
        
        // Forecast
        if (forecastDays > 0 && weatherData.has("daily")) {
            JsonObject daily = weatherData.getAsJsonObject("daily");
            result.append("\n📅 Forecast:\n");
            
            com.google.gson.JsonArray dates = daily.getAsJsonArray("time");
            com.google.gson.JsonArray maxTemps = daily.getAsJsonArray("temperature_2m_max");
            com.google.gson.JsonArray minTemps = daily.getAsJsonArray("temperature_2m_min");
            com.google.gson.JsonArray weatherCodes = daily.getAsJsonArray("weather_code");
            com.google.gson.JsonArray precipitation = daily.getAsJsonArray("precipitation_sum");
            
            for (int i = 0; i < Math.min(forecastDays, dates.size()); i++) {
                String date = dates.get(i).getAsString();
                double maxTemp = maxTemps.get(i).getAsDouble();
                double minTemp = minTemps.get(i).getAsDouble();
                int code = weatherCodes.get(i).getAsInt();
                double precip = precipitation.get(i).getAsDouble();
                
                result.append(String.format("  %s: %s, %.0f°C / %.0f°C", 
                    date, getWeatherDescription(code), maxTemp, minTemp));
                
                if (precip > 0) {
                    result.append(String.format(", 💧 %.1f mm", precip));
                }
                result.append("\n");
            }
        }
        
        return result.toString();
    }
    
    /**
     * Convert WMO weather code to description
     */
    private String getWeatherDescription(int code) {
        switch (code) {
            case 0: return "☀️ Clear";
            case 1:
            case 2: return "🌤️ Partly Cloudy";
            case 3: return "☁️ Overcast";
            case 45:
            case 48: return "🌫️ Foggy";
            case 51:
            case 53:
            case 55: return "🌧️ Light Drizzle";
            case 61:
            case 63:
            case 65: return "🌧️ Rain";
            case 71:
            case 73:
            case 75: return "❄️ Snow";
            case 77: return "❄️ Snow Grains";
            case 80:
            case 81:
            case 82: return "⛈️ Shower";
            case 85:
            case 86: return "❄️ Snow Shower";
            case 95:
            case 96:
            case 99: return "⛈️ Thunderstorm";
            default: return "🌡️ Weather";
        }
    }
    
    @Override
    public boolean validateInput(JsonObject input) {
        try {
            return input.has("city") 
                && input.get("city").isJsonPrimitive()
                && !input.get("city").getAsString().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
