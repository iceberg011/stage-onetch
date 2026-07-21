package com.example.demo.Repository;

import com.example.demo.Entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserAccount, Long> {
    
    // ===== BASIC FIND METHODS =====
    Optional<UserAccount> findByUsername(String username);
    Optional<UserAccount> findByEmail(String email);
    
    // ===== REMEMBER ME METHODS - Using @Query to match the entity field name =====
    
    // Find by session_key (using @Query with the exact field name from entity)
    @Query("SELECT u FROM UserAccount u WHERE u.session_key = :sessionKey")
    Optional<UserAccount> findBySessionKey(@Param("sessionKey") String sessionKey);
    
    // Find user by token (session_key starts with token)
    @Query("SELECT u FROM UserAccount u WHERE u.session_key LIKE CONCAT(:token, '%')")
    Optional<UserAccount> findByRememberToken(@Param("token") String token);
    
    // ===== EXISTENCE CHECKS =====
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    // ===== UPDATE METHODS =====
    @Modifying
    @Transactional
    @Query("UPDATE UserAccount u SET u.session_key = :sessionKey WHERE u.id = :userId")
    void updateSessionKey(@Param("userId") Long userId, @Param("sessionKey") String sessionKey);
    
    @Modifying
    @Transactional
    @Query("UPDATE UserAccount u SET u.session_key = null WHERE u.id = :userId")
    void clearSessionKey(@Param("userId") Long userId);
}