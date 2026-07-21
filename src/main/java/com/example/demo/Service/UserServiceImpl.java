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
import java.util.ArrayList;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import com.example.demo.DTO.LoginRequest;
import com.example.demo.DTO.SignupRequest;
import com.example.demo.Entity.UserAccount;
import com.example.demo.Repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            
            userRepository.save(user);
            logger.info("User saved successfully with ID: {}", user.getId());
            
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
    public boolean validateAuthenticationByEmail(String email, String password) {
        logger.info("validateAuthenticationByEmail called with: {}", email);
        if (email == null || password == null) {
            logger.info("Email or password is null");
            return false;
        }
        
        UserAccount user = getUserByEmail(email);
        if (user == null) {
            logger.info("User not found for email: {}", email);
            return false;
        }
        
        boolean matches = passwordEncoder.matches(password, user.getPassword());
        logger.info("Password matches: {}", matches);
        return matches;
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
    @Transactional
    public void updateUserLoginInfo(UserAccount user) {
        logger.info("Updating login info for user: {}", user.getUsername());
        
        Integer currentCount = user.getLogin_count();
        if (currentCount == null) {
            currentCount = 0;
        }
        user.setLogin_count(currentCount + 1);
        user.setLast_login(LocalDateTime.now());
        user.setUpdate_time(LocalDateTime.now());
        
        userRepository.save(user);
        logger.info("Login info updated successfully. New login count: {}", user.getLogin_count());
    }


    // ===== REMEMBER ME METHODS =====
    @Override
    public UserAccount findBySessionKey(String sessionKey) {
        logger.info("Finding user by session key: {}", sessionKey);
        Optional<UserAccount> user = userRepository.findBySessionKey(sessionKey);
        return user.orElse(null);
    }

    @Override
    public UserAccount findByRememberToken(String token) {
        logger.info("Finding user by remember token: {}", token);
        Optional<UserAccount> user = userRepository.findByRememberToken(token);
        return user.orElse(null);
    }

    @Override
    @Transactional
    public void saveSessionKey(Long userId, String sessionKey) {
        logger.info("Saving session key for user ID: {}", userId);
        userRepository.updateSessionKey(userId, sessionKey);
        logger.info("Session key saved successfully");
    }

    @Override
    @Transactional
    public void clearSessionKey(Long userId) {
        logger.info("Clearing session key for user ID: {}", userId);
        userRepository.clearSessionKey(userId);
        logger.info("Session key cleared successfully");
    }




    
      // ===== SEARCH METHODS IMPLEMENTATIONS =====
    
    @Override
    public List<UserAccount> searchUsers(String searchTerm, String field, String sort, String role, String status) {
        logger.info("=== searchUsers called ===");
        logger.info("Search Term: {}", searchTerm);
        logger.info("Field: {}", field);
        logger.info("Sort: {}", sort);
        logger.info("Role: {}", role);
        logger.info("Status: {}", status);
        
        // Get all users
        List<UserAccount> allUsers = userRepository.findAll();
        List<UserAccount> result = new ArrayList<>(allUsers);
        
        // Apply search
        if (searchTerm != null && !searchTerm.isEmpty()) {
            result = searchUsersByKeyword(result, searchTerm, field);
        }
        
        // Apply role filter
        if (role != null && !role.isEmpty() && !role.equals("all")) {
            result = filterUsersByRole(result, role);
        }
        
        // Apply status filter
        if (status != null && !status.isEmpty() && !status.equals("all")) {
            result = filterUsersByStatus(result, status);
        }
        
        // Apply sorting
        if (sort != null && !sort.isEmpty()) {
            String sortField = (field != null && !field.isEmpty() && !field.equals("all")) ? field : "first_name";
            result = sortUsers(result, sortField, sort);
        }
        
        logger.info("Search results count: {}", result.size());
        return result;
    }

    @Override
    public List<UserAccount> searchUsersByKeyword(String keyword) {
        logger.info("=== searchUsersByKeyword called with: {} ===", keyword);
        List<UserAccount> allUsers = userRepository.findAll();
        return searchUsersByKeyword(allUsers, keyword, null);
    }

    private List<UserAccount> searchUsersByKeyword(List<UserAccount> users, String keyword, String field) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return users;
        }
        
        String keywordLower = keyword.toLowerCase().trim();
        List<UserAccount> result = new ArrayList<>();
        
        for (UserAccount user : users) {
            boolean match = false;
            
            if (field == null || field.isEmpty() || field.equals("all")) {
                // Search in all fields
                match = (user.getFirst_name() != null && user.getFirst_name().toLowerCase().contains(keywordLower)) ||
                        (user.getLast_name() != null && user.getLast_name().toLowerCase().contains(keywordLower)) ||
                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(keywordLower)) ||
                        (user.getUsername() != null && user.getUsername().toLowerCase().contains(keywordLower)) ||
                        (user.gettele_phone() != null && user.gettele_phone().toString().contains(keywordLower));
            } else {
                // Search in specific field
                switch (field) {
                    case "first_name":
                        match = user.getFirst_name() != null && user.getFirst_name().toLowerCase().contains(keywordLower);
                        break;
                    case "last_name":
                        match = user.getLast_name() != null && user.getLast_name().toLowerCase().contains(keywordLower);
                        break;
                    case "email":
                        match = user.getEmail() != null && user.getEmail().toLowerCase().contains(keywordLower);
                        break;
                    case "username":
                        match = user.getUsername() != null && user.getUsername().toLowerCase().contains(keywordLower);
                        break;
                    case "phone":
                        match = user.gettele_phone() != null && user.gettele_phone().toString().contains(keywordLower);
                        break;
                    default:
                        match = false;
                }
            }
            
            if (match) {
                result.add(user);
            }
        }
        
        return result;
    }

    @Override
    public List<UserAccount> filterUsersByRole(String role) {
        logger.info("=== filterUsersByRole called with: {} ===", role);
        List<UserAccount> allUsers = userRepository.findAll();
        return filterUsersByRole(allUsers, role);
    }

    private List<UserAccount> filterUsersByRole(List<UserAccount> users, String role) {
        if (role == null || role.isEmpty() || role.equals("all")) {
            return users;
        }
        
        List<UserAccount> result = new ArrayList<>();
        
        for (UserAccount user : users) {
            boolean match = false;
            switch (role.toLowerCase()) {
                case "admin":
                    match = user.getIs_superuser();
                    break;
                case "staff":
                    match = user.getIs_staff();
                    break;
                case "user":
                    match = !user.getIs_superuser() && !user.getIs_staff();
                    break;
                default:
                    match = false;
            }
            
            if (match) {
                result.add(user);
            }
        }
        
        return result;
    }

    @Override
    public List<UserAccount> filterUsersByStatus(String status) {
        logger.info("=== filterUsersByStatus called with: {} ===", status);
        List<UserAccount> allUsers = userRepository.findAll();
        return filterUsersByStatus(allUsers, status);
    }

    private List<UserAccount> filterUsersByStatus(List<UserAccount> users, String status) {
        if (status == null || status.isEmpty() || status.equals("all")) {
            return users;
        }
        
        boolean isActive = status.equalsIgnoreCase("active");
        List<UserAccount> result = new ArrayList<>();
        
        for (UserAccount user : users) {
            if (user.getIs_active() == isActive) {
                result.add(user);
            }
        }
        
        return result;
    }

    @Override
    public List<UserAccount> sortUsers(List<UserAccount> users, String field, String sortDirection) {
        logger.info("=== sortUsers called with field: {}, direction: {} ===", field, sortDirection);
        
        if (users == null || users.isEmpty()) {
            return users;
        }
        
        boolean ascending = sortDirection == null || sortDirection.equals("asc");
        String sortField = (field != null && !field.isEmpty()) ? field : "first_name";
        
        List<UserAccount> result = new ArrayList<>(users);
        
        switch (sortField) {
            case "first_name":
                if (ascending) {
                    result.sort((u1, u2) -> compareStrings(u1.getFirst_name(), u2.getFirst_name()));
                } else {
                    result.sort((u1, u2) -> compareStrings(u2.getFirst_name(), u1.getFirst_name()));
                }
                break;
            case "last_name":
                if (ascending) {
                    result.sort((u1, u2) -> compareStrings(u1.getLast_name(), u2.getLast_name()));
                } else {
                    result.sort((u1, u2) -> compareStrings(u2.getLast_name(), u1.getLast_name()));
                }
                break;
            case "email":
                if (ascending) {
                    result.sort((u1, u2) -> compareStrings(u1.getEmail(), u2.getEmail()));
                } else {
                    result.sort((u1, u2) -> compareStrings(u2.getEmail(), u1.getEmail()));
                }
                break;
            case "username":
                if (ascending) {
                    result.sort((u1, u2) -> compareStrings(u1.getUsername(), u2.getUsername()));
                } else {
                    result.sort((u1, u2) -> compareStrings(u2.getUsername(), u1.getUsername()));
                }
                break;
            case "id":
                if (ascending) {
                    result.sort((u1, u2) -> u1.getId().compareTo(u2.getId()));
                } else {
                    result.sort((u1, u2) -> u2.getId().compareTo(u1.getId()));
                }
                break;
            default:
                // Default sort by first_name
                if (ascending) {
                    result.sort((u1, u2) -> compareStrings(u1.getFirst_name(), u2.getFirst_name()));
                } else {
                    result.sort((u1, u2) -> compareStrings(u2.getFirst_name(), u1.getFirst_name()));
                }
                break;
        }
        
        return result;
    }

    private int compareStrings(String s1, String s2) {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return -1;
        if (s2 == null) return 1;
        return s1.compareToIgnoreCase(s2);
    }
}