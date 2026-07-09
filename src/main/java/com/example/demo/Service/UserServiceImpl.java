package com.example.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.DTO.LoginRequest;
import com.example.demo.DTO.SignupRequest;
import com.example.demo.Entity.UserAccount;
import com.example.demo.Repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        logger.info("=== UserServiceImpl CREATED ===");
    }

    @Override
    @Transactional
    public void saveUser(UserAccount user) {
        try {
            logger.info("=== SAVING USER ===");
            logger.info("Username: {}", user.getUsername());
            logger.info("Email: {}", user.getEmail());
            logger.info("First Name: {}", user.getFirst_name());
            logger.info("Last Name: {}", user.getLast_name());
            logger.info("Date Join: {}", user.getDate_join());
            logger.info("Login Count: {}", user.getLogin_count());
            logger.info("Is Active: {}", user.getIs_active());
            
            // Encrypt password
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            logger.info("Password encoded successfully");
            
            // Save to database
            UserAccount savedUser = userRepository.save(user);
            logger.info("User saved successfully with ID: {}", savedUser.getId());
            
        } catch (Exception e) {
            logger.error("Error saving user: ", e);
            throw e;
        }
    }

    @Override
    public UserAccount getUserByUsername(String username) {
        Optional<UserAccount> user = userRepository.findByUsername(username);
        return user.orElse(null);
    }

    @Override
    public UserAccount getUserByEmail(String email) {
        logger.info("Getting user by email: {}", email);
        Optional<UserAccount> user = userRepository.findByEmail(email);
        return user.orElse(null);
    }

    @Override
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean validateAuthentication(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        UserAccount user = getUserByUsername(username);
        if (user == null) {
            return false;
        }
        return passwordEncoder.matches(password, user.getPassword());
    }


    @Override
    public Optional<UserAccount> authenticateUser(LoginRequest request) {
        Optional<UserAccount> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        UserAccount user = userOpt.get();
        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public UserAccount registerUser(SignupRequest request) {
        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirst_name(request.getFirstName());
        user.setLast_name(request.getLastName());
        user.settele_phone(request.gettele_phone());
        
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);
        
        return userRepository.save(user);
    }

    @Override
    public boolean validateAuthenticationByEmail(String email, String password) {
        System.out.println("validateAuthenticationByEmail called with: " + email);
        if (email == null || password == null) {
            System.out.println("Email or password is null");
            return false;
        }
        
        UserAccount user = getUserByEmail(email);
        if (user == null) {
            System.out.println("User not found for email: " + email);
            return false;
        }
        
        boolean matches = passwordEncoder.matches(password, user.getPassword());
        System.out.println("Password matches: " + matches);
        return matches;
    }

    @Override
    @Transactional
    public void updateUserLoginInfo(UserAccount user) {
        System.out.println("Updating login info for user: " + user.getUsername());
        
        // Increment login count
        Integer currentCount = user.getLogin_count();
        if (currentCount == null) {
            currentCount = 0;
        }
        user.setLogin_count(currentCount + 1);
        
        // Update last login time
        user.setLast_login(LocalDateTime.now());
        
        // Update the update_time (audit field)
        user.setUpdate_time(LocalDateTime.now());
        
        // Save the user
        userRepository.save(user);
        System.out.println("Login info updated. New login count: " + user.getLogin_count());
    }
}