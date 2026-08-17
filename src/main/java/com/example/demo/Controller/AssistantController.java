package com.example.demo.Controller;

import com.example.demo.Service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/assistant")
public class AssistantController {

    private static final Logger logger = LoggerFactory.getLogger(AssistantController.class);

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> ask(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        
        logger.info("📝 Received question: {}", question);
        
        if (question == null || question.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("answer", "Please ask a question.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            String contextualQuestion = buildContextualPrompt(question);
            String answer = geminiService.askGemini(contextualQuestion);
            
            Map<String, Object> response = new HashMap<>();
            
            if (answer != null && !answer.isEmpty()) {
                response.put("answer", answer);
            } else {
                response.put("answer", "I apologize, but I'm having trouble connecting to my AI services right now. Please try again later.");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Error: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("answer", "I apologize, but I encountered an error. Please try again later.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/models")
    public ResponseEntity<Map<String, Object>> listModels() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("models", geminiService.getSupportedModels());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        try {
            String testQuestion = "Hello, are you working? Please respond with a simple greeting.";
            String answer = geminiService.askGemini(testQuestion);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            response.put("message", "Test completed");
            response.put("answer", answer != null ? answer : "I'm here! 😊");
            response.put("models", geminiService.getSupportedModels());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Assistant is alive!");
    }

    private String buildContextualPrompt(String question) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are the OneTech AI Assistant, a knowledgeable and friendly technology expert. ");
        prompt.append("OneTech is a technology company specializing in cloud infrastructure, AI & Machine Learning, ");
        prompt.append("Enterprise Security, Real-time Analytics, Global Network Solutions, and 24/7 Support. ");
        prompt.append("Keep your answers helpful, friendly, and concise.\n\n");
        prompt.append("Question: " + question + "\n\n");
        prompt.append("Answer:");

        return prompt.toString();
    }
}