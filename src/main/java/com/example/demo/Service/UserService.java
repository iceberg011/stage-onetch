package com.example.demo.Service;

import com.example.demo.DTO.LoginRequest;
import com.example.demo.DTO.SignupRequest;
import com.example.demo.Entity.UserAccount;

import java.util.Optional;

public interface UserService {
    
    // ===== BASIC CRUD =====
    void saveUser(UserAccount user);
    UserAccount getUserByUsername(String username);
    UserAccount getUserByEmail(String email);
    
    // ===== EXISTENCE CHECKS =====
    boolean userExists(String username);
    boolean userExistsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    // ===== AUTHENTICATION =====
    boolean validateAuthentication(String username, String password);
    boolean validateAuthenticationByEmail(String email, String password);
    Optional<UserAccount> authenticateUser(LoginRequest request);
    
    // ===== REGISTRATION =====
    UserAccount registerUser(SignupRequest request);
    
    // ===== USER MANAGEMENT =====
    void updateUserLoginInfo(UserAccount user);
}