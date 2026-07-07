package com.example.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.DTO.LoginRequest;
import com.example.demo.DTO.SignupRequest;
import com.example.demo.Entity.UserAccount;
import com.example.demo.Repository.UserRepository;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        System.out.println("=== UserServiceImpl CREATED ===");
    }

    @Override
    public void saveUser(UserAccount user) {
        System.out.println("Saving user: " + user.getUsername());
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);
        System.out.println("User saved successfully!");
    }

    @Override
    public UserAccount getUserByUsername(String username) {
        Optional<UserAccount> user = userRepository.findByUsername(username);
        return user.orElse(null);
    }

    @Override
    public UserAccount getUserByEmail(String email) {
        System.out.println("Getting user by email: " + email);
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

    // THIS IS THE METHOD THAT WAS MISSING - NOW IMPLEMENTED
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
    public UserAccount registerUser(SignupRequest request) {
        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirst_name(request.getFirstName());
        user.setLast_name(request.getLastName());
        user.setPhone_number(request.getPhoneNumber());
        
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);
        
        return userRepository.save(user);
    }
}