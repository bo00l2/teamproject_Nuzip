package com.teamproject.GeminiAPI;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class GeminiConfig {
    private final Dotenv dotenv = Dotenv.load();

    public String getApiKey() { return dotenv.get("GEMINI_API_KEY"); }
    public String getModel() { return "gemini-2.0-flash"; }
    public double getTemperature() { return 0.5; }
    public int getMaxTokens() { return 1000; }
    public int getTimeoutSeconds() { return 30; }
}
