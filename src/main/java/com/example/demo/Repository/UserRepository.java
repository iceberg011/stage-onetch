package com.example.demo.Repository;

import com.example.demo.Entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserAccount, Long> {
    
    // Find user by username
    Optional<UserAccount> findByUsername(String username);
    
    // Find user by email
    Optional<UserAccount> findByEmail(String email);
    
    // Check if username exists
    boolean existsByUsername(String username);
    
    // Check if email exists
    boolean existsByEmail(String email);
}