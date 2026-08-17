package com.example.demo.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Run this test to diagnose Gemini API connectivity
 * java -cp target/demo-0.0.1-SNAPSHOT.jar com.example.demo.Controller.GeminiDiagnosticTest
 */
public class GeminiDiagnosticTest {

    private static final String API_KEY = "AIzaSyB_bPifGUywgsAcwXcnkW4TCC6MkjykRjc";
    private static final String MODEL = "gemini-1.5-flash";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("OneTech Chatbot - Gemini API Diagnostic");
        System.out.println("========================================\n");

        // Test 1: Check API Key
        System.out.println("[1] Checking API Key...");
        if (API_KEY != null && !API_KEY.isEmpty()) {
            System.out.println("✅ API Key configured");
            System.out.println("   Key starts with: " + API_KEY.substring(0, 10) + "...");
        } else {
            System.out.println("❌ API Key not configured!");
            return;
        }
        System.out.println();

        // Test 2: Build request
        System.out.println("[2] Building API Request...");
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + 
                    ":generateContent?key=" + API_KEY;
        System.out.println("   URL: " + url);
        System.out.println("✅ Request built successfully");
        System.out.println();

        // Test 3: Send request
        System.out.println("[3] Sending API Request...");
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            Map<String, Object> requestBody = new LinkedHashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new LinkedHashMap<>();
            List<Map<String, Object>> parts = new ArrayList<>();
            Map<String, Object> part = new LinkedHashMap<>();
            part.put("text", "Hello, are you working?");
            parts.add(part);
            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            String response = restTemplate.postForObject(url, request, String.class);
            
            System.out.println("✅ Request successful!");
            System.out.println();

            // Test 4: Parse response
            System.out.println("[4] Parsing Response...");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response);

            if (json.has("error")) {
                System.out.println("❌ API Error!");
                JsonNode error = json.get("error");
                System.out.println("   Code: " + error.get("code").asText());
                System.out.println("   Message: " + error.get("message").asText());
                
                String code = error.get("code").asText();
                if (code.contains("UNAUTHENTICATED") || code.contains("401")) {
                    System.out.println("   💡 Fix: Check your API key");
                } else if (code.contains("PERMISSION_DENIED") || code.contains("403")) {
                    System.out.println("   💡 Fix: Enable Generative Language API in Google Cloud");
                } else if (code.contains("RESOURCE_EXHAUSTED") || code.contains("429")) {
                    System.out.println("   💡 Fix: Rate limited - try again later");
                }
            } else if (json.has("candidates")) {
                System.out.println("✅ AI responded successfully!");
                JsonNode candidate = json.get("candidates").get(0);
                if (candidate.has("content")) {
                    String answer = candidate.get("content").get("parts").get(0).get("text").asText();
                    System.out.println("   Answer: " + answer);
                }
            } else {
                System.out.println("⚠️  Unexpected response format");
                System.out.println(response);
            }

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.out.println("❌ HTTP Error " + e.getStatusCode().value());
            System.out.println("   Message: " + e.getMessage());
            System.out.println("   Response: " + e.getResponseBodyAsString());
            
            int code = e.getStatusCode().value();
            if (code == 401 || code == 403) {
                System.out.println("   💡 Authentication/Permission issue");
            } else if (code == 429) {
                System.out.println("   💡 Rate limit exceeded");
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println();
        System.out.println("========================================");
        System.out.println("Diagnostic test complete");
        System.out.println("========================================");
    }
}
