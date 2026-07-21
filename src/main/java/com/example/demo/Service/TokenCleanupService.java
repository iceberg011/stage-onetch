package com.example.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Repository.UserRepository;

@Service
public class TokenCleanupService {

    @Autowired
    private UserRepository userRepository;

    // Run every hour to clean expired tokens
    @Scheduled(fixedDelay = 3600000) // 1 hour
    @Transactional
    public void cleanupExpiredTokens() {
        System.out.println("Running token cleanup job...");
        // Find all users with session_key
        // Check each token expiry
        // Clear expired ones
        // This is optional - you can implement if needed
    }
}