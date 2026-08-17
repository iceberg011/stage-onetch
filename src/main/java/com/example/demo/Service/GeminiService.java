package com.example.demo.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;

@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // ===== ALL AVAILABLE GEMINI MODELS =====
    private static final String[] MODELS = {
        // Latest models (priority order)
        "gemini-2.0-flash-exp",
        "gemini-2.0-flash",
        "gemini-1.5-pro",
        "gemini-1.5-flash",
        "gemini-pro",
        "gemini-pro-vision",
        "gemini-1.0-pro"
    };

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 500;

    public GeminiService(RestTemplateBuilder builder) {
        this.restTemplate = builder
            .setConnectTimeout(Duration.ofSeconds(10))
            .setReadTimeout(Duration.ofSeconds(30))
            .build();
        this.objectMapper = new ObjectMapper();
    }

    public String askGemini(String question) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.error("❌ API key not configured!");
            logger.info("⚠️  Using fallback response system");
            return generateFallbackResponse(question);
        }

        logger.info("📨 Processing question: {}", question.substring(0, Math.min(50, question.length())));
        
        // Try each model in order
        for (String model : MODELS) {
            String result = tryModel(model, question);
            if (result != null) {
                return result;
            }
        }

        logger.error("🚫 All models failed. Attempted: {}", String.join(", ", MODELS));
        logger.info("⚠️  Falling back to local response generator");
        return generateFallbackResponse(question);
    }

    private String generateFallbackResponse(String question) {
        String msg = question.toLowerCase();
        
        // Company/Service Related
        if (msg.contains("service") || msg.contains("offer") || msg.contains("provide")) {
            return "🚀 OneTech offers a comprehensive range of technology services including:\n\n" +
                   "• Cloud Infrastructure & Hosting\n" +
                   "• AI-Powered Analytics & Insights\n" +
                   "• Enterprise-Grade Security Solutions\n" +
                   "• Real-Time Data Processing\n" +
                   "• Global Network Deployment\n" +
                   "• 24/7 Technical Support\n\n" +
                   "Which service interests you most?";
        }
        
        // Account/Registration
        if (msg.contains("account") || msg.contains("sign up") || msg.contains("register") || msg.contains("create")) {
            return "📝 Creating an account with OneTech is easy!\n\n" +
                   "1. Click the 'Sign Up' button\n" +
                   "2. Enter your email and password\n" +
                   "3. Verify your email\n" +
                   "4. Complete your profile\n" +
                   "5. Start using OneTech!\n\n" +
                   "Need help? Contact support@onetech.com";
        }
        
        // Support
        if (msg.contains("support") || msg.contains("help") || msg.contains("assist")) {
            return "💬 Our support team is ready to assist you!\n\n" +
                   "• 24/7 Live Chat Support\n" +
                   "• Email: support@onetech.com\n" +
                   "• Phone: +1 (555) 123-4567\n" +
                   "• Knowledge Base & Documentation\n\n" +
                   "How can we help you today?";
        }
        
        // Company Information
        if (msg.contains("about") || msg.contains("what is") || msg.contains("who is") || msg.contains("company")) {
            return "🌟 About OneTech\n\n" +
                   "OneTech is a leading technology solutions provider dedicated to helping businesses succeed in the digital age.\n\n" +
                   "We specialize in:\n" +
                   "• Cutting-edge cloud infrastructure\n" +
                   "• AI and machine learning solutions\n" +
                   "• Enterprise security\n" +
                   "• Real-time business analytics\n\n" +
                   "Our mission: Empower organizations with innovative technology.";
        }
        
        // Pricing
        if (msg.contains("price") || msg.contains("cost") || msg.contains("pricing") || msg.contains("plan")) {
            return "💰 OneTech Pricing Plans\n\n" +
                   "• Starter: $29/month - Perfect for startups\n" +
                   "• Professional: $99/month - For growing teams\n" +
                   "• Enterprise: Custom pricing - Full suite\n\n" +
                   "All plans include:\n" +
                   "• 14-day free trial\n" +
                   "• Priority support\n" +
                   "• Regular updates\n\n" +
                   "Ready to get started?";
        }
        
        // Security
        if (msg.contains("security") || msg.contains("secure") || msg.contains("protection") || msg.contains("safe")) {
            return "🔒 Security at OneTech\n\n" +
                   "Your data security is our top priority.\n\n" +
                   "We provide:\n" +
                   "• End-to-end encryption\n" +
                   "• SOC 2 Type II compliance\n" +
                   "• Regular security audits\n" +
                   "• Multi-factor authentication\n" +
                   "• GDPR & HIPAA compliance\n\n" +
                   "Your data is always safe with OneTech!";
        }
        
        // Integration
        if (msg.contains("integrate") || msg.contains("integration") || msg.contains("api")) {
            return "🔌 OneTech Integrations\n\n" +
                   "OneTech seamlessly integrates with:\n\n" +
                   "• CRM: Salesforce, HubSpot\n" +
                   "• Communication: Slack, Microsoft Teams\n" +
                   "• Analytics: Google Analytics, Tableau\n" +
                   "• Custom API integration available\n\n" +
                   "Need a specific integration? Contact support!";
        }
        
        // Greeting
        if (msg.contains("hello") || msg.contains("hi") || msg.contains("hey") || msg.contains("greet")) {
            return "👋 Welcome to OneTech!\n\n" +
                   "I'm your AI assistant, here to help you explore our platform.\n\n" +
                   "I can help with:\n" +
                   "• Services & Features\n" +
                   "• Account Creation\n" +
                   "• Pricing Plans\n" +
                   "• Security Information\n" +
                   "• Technical Support\n" +
                   "• Integrations\n\n" +
                   "What would you like to know?";
        }
        
        // Default response
        return "🤔 Great question!\n\n" +
               "I'm currently experiencing connection issues with the AI service, but I can still help with common topics:\n\n" +
               "• Services & Features\n" +
               "• Pricing Plans\n" +
               "• Account Information\n" +
               "• Security & Compliance\n" +
               "• Technical Support\n\n" +
               "For specialized questions, please contact our support team at support@onetech.com";
    }

    private String tryModel(String model, String question) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                logger.info("🔄 Attempting model: {} (Attempt {}/{})", model, attempt, MAX_RETRIES);
                
                String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + 
                            ":generateContent?key=" + apiKey;

                Map<String, Object> requestBody = buildRequestBody(question);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("User-Agent", "OneTech-Assistant/1.0");

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                try {
                    ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        request,
                        String.class
                    );

                    logger.debug("Response status: {}", response.getStatusCode());

                    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                        String answer = parseResponse(response.getBody());
                        if (answer != null && !answer.isEmpty()) {
                            logger.info("✅ Successfully used model: {}", model);
                            return answer;
                        } else {
                            logger.warn("⚠️  Model {} returned empty response", model);
                        }
                    } else {
                        logger.warn("❌ Model {} returned status: {}", model, response.getStatusCode());
                    }
                    
                } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
                    logger.warn("⏱️  Rate limit hit on model {}. Waiting before retry...", model);
                    logger.debug("Rate limit response: {}", e.getResponseBodyAsString());
                    if (attempt < MAX_RETRIES) {
                        try {
                            Thread.sleep(RETRY_DELAY_MS * attempt);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized e) {
                    logger.error("❌ Model {} - Authentication Failed (401): Invalid API key or insufficient permissions", model);
                    logger.error("API Key starts with: {}", apiKey.substring(0, Math.min(10, apiKey.length())) + "...");
                    logger.debug("Auth error response: {}", e.getResponseBodyAsString());
                    return null;
                } catch (org.springframework.web.client.HttpClientErrorException.Forbidden e) {
                    logger.error("❌ Model {} - Access Forbidden (403): Check API enablement in Google Cloud", model);
                    logger.debug("Forbidden error response: {}", e.getResponseBodyAsString());
                    return null;
                } catch (org.springframework.web.client.HttpClientErrorException e) {
                    logger.error("❌ Model {} - Client Error {}: {}", 
                        model, e.getStatusCode().value(), e.getResponseBodyAsString());
                    logger.debug("Full error response: {}", e.getResponseBodyAsString());
                    // Don't retry on 4xx errors except 429
                    return null;
                } catch (org.springframework.web.client.HttpServerErrorException e) {
                    logger.warn("⚠️  Model {} - Server Error {}: {}", model, e.getStatusCode().value(), e.getMessage());
                    logger.debug("Server error response: {}", e.getResponseBodyAsString());
                    if (attempt < MAX_RETRIES) {
                        try {
                            Thread.sleep(RETRY_DELAY_MS * attempt);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                } catch (org.springframework.web.client.ResourceAccessException e) {
                    logger.warn("⚠️  Model {} - Connection error: {}", model, e.getMessage());
                    if (attempt < MAX_RETRIES) {
                        try {
                            Thread.sleep(RETRY_DELAY_MS * attempt);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                
            } catch (Exception e) {
                logger.error("❌ Model {} - Unexpected error on attempt {}: {}", model, attempt, e.getMessage(), e);
            }
        }
        
        return null;
    }

    private Map<String, Object> buildRequestBody(String question) {
        Map<String, Object> requestBody = new LinkedHashMap<>();

        // Build contents array with proper structure
        List<Map<String, Object>> contentsList = new ArrayList<>();
        
        Map<String, Object> content = new LinkedHashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();
        
        Map<String, Object> part = new LinkedHashMap<>();
        part.put("text", question);
        parts.add(part);
        
        content.put("parts", parts);
        contentsList.add(content);
        
        requestBody.put("contents", contentsList);

        // Safety settings to prevent blocking
        Map<String, Object> safetySettings = new LinkedHashMap<>();
        List<Map<String, Object>> safetyList = new ArrayList<>();
        
        Map<String, Object> safety = new LinkedHashMap<>();
        safety.put("category", "HARM_CATEGORY_UNSPECIFIED");
        safety.put("threshold", "BLOCK_NONE");
        safetyList.add(safety);
        
        requestBody.put("safetySettings", safetyList);

        // Generation configuration
        Map<String, Object> generationConfig = new LinkedHashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 1024);
        generationConfig.put("topP", 0.95);
        generationConfig.put("topK", 64);
        requestBody.put("generationConfig", generationConfig);

        return requestBody;
    }

    private String parseResponse(String responseBody) {
        try {
            if (responseBody == null || responseBody.trim().isEmpty()) {
                logger.warn("⚠️  Empty response body received");
                return null;
            }

            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            // Check for errors first
            if (jsonNode.has("error")) {
                JsonNode error = jsonNode.get("error");
                String message = error.has("message") ? error.get("message").asText() : "Unknown error";
                String code = error.has("code") ? error.get("code").asText() : "UNKNOWN";
                logger.error("❌ Gemini API error ({}): {}", code, message);
                return null;
            }
            
            // Handle candidates
            if (jsonNode.has("candidates")) {
                JsonNode candidates = jsonNode.get("candidates");
                if (candidates.size() == 0) {
                    logger.warn("⚠️  No candidates in response");
                    return null;
                }

                JsonNode candidate = candidates.get(0);
                
                // Check for finish reason warnings
                if (candidate.has("finishReason")) {
                    String finishReason = candidate.get("finishReason").asText();
                    if ("SAFETY".equals(finishReason)) {
                        logger.warn("⚠️  Response blocked for safety reasons");
                        return null;
                    }
                    if ("OTHER".equals(finishReason)) {
                        logger.warn("⚠️  Response stopped for other reasons");
                        return null;
                    }
                }
                
                // Extract text content
                if (candidate.has("content") && candidate.get("content").has("parts")) {
                    JsonNode parts = candidate.get("content").get("parts");
                    if (parts.size() > 0 && parts.get(0).has("text")) {
                        String answer = parts.get(0).get("text").asText().trim();
                        if (!answer.isEmpty()) {
                            logger.info("✅ Successfully parsed response");
                            return answer;
                        }
                    }
                }
            }
            
            logger.warn("⚠️  No valid text content found in response. Structure: {}", 
                responseBody.substring(0, Math.min(300, responseBody.length())));
            return null;
            
        } catch (Exception e) {
            logger.error("❌ Error parsing response: {}", e.getMessage(), e);
            return null;
        }
    }

    public List<String> getSupportedModels() {
        return Arrays.asList(MODELS);
    }
}