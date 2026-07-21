package com.example.demo.Service;

import com.example.demo.DTO.LoginRequest;
import com.example.demo.DTO.SignupRequest;
import com.example.demo.Entity.UserAccount;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

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
    
    // ===== REMEMBER ME (using session_key) =====
    UserAccount findBySessionKey(String sessionKey);
    UserAccount findByRememberToken(String token);
    void saveSessionKey(Long userId, String sessionKey);
    void clearSessionKey(Long userId);



    List<UserAccount> searchUsers(String searchTerm, String field, String sort, String role, String status);
    List<UserAccount> searchUsersByKeyword(String keyword);
    List<UserAccount> filterUsersByRole(String role);
    List<UserAccount> filterUsersByStatus(String status);
    List<UserAccount> sortUsers(List<UserAccount> users, String field, String sortDirection);
}