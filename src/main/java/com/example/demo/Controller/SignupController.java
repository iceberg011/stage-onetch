package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Entity.UserAccount;
import com.example.demo.Service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class SignupController {

    private static final Logger logger = LoggerFactory.getLogger(SignupController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/signup")
    public String showSignupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam("first_name") String firstName,
            @RequestParam("last_name") String lastName,
            @RequestParam("tele_phone") Integer tele_phone,
            Model model) {
        
        logger.info("=== SIGNUP ATTEMPT ===");
        logger.info("Email: {}", email);
        logger.info("First Name: {}", firstName);
        logger.info("Last Name: {}", lastName);
        logger.info("Phone: {}", tele_phone);
        
        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            logger.warn("Passwords do not match");
            model.addAttribute("error", "Passwords do not match");
            return "signup";
        }
        
        // Generate username from first name and last name
        String generatedUsername = generateUsername(firstName, lastName);
        logger.info("Generated username: {}", generatedUsername);
        
        // Check if generated username already exists
        if (userService.userExists(generatedUsername)) {
            logger.warn("Generated username already exists: {}", generatedUsername);
            model.addAttribute("error", "Username already exists. Please try different name.");
            return "signup";
        }
        
        // Check if email already exists
        if (userService.userExistsByEmail(email)) {
            logger.warn("Email already exists: {}", email);
            model.addAttribute("error", "Email already exists");
            return "signup";
        }
        
        try {
            // Create new user with all fields initialized
            UserAccount user = new UserAccount();
            
            // Basic user information
            user.setUsername(generatedUsername); // Set the generated username
            user.setEmail(email);
            user.setPassword(password); // Will be encrypted in service's saveUser method
            user.setFirst_name(firstName);
            user.setLast_name(lastName);
            user.settele_phone(tele_phone);
            
            user.setDate_join(LocalDate.now());
            
            user.setUpdate_time(LocalDateTime.now());
            
            user.setLogin_count(0);
            user.setLogin_ip(0);
            user.setLogin_id(0);
            user.setLogin_type("email");
            user.setLast_login(null);
            
            user.setIs_staff(false);
            user.setIs_active(true);
            user.setIs_superuser(false);
            user.setIs_public(false);
            user.setCan_manage_all_dept(false);
            
            user.setPhoto("/static/user/default.png");
            
            user.setEmp_pin(null);
            user.setDel_flag(0);
            user.setSession_key(null);
            user.settele_phone(null);
            
            logger.info("User object created: {}", user);
            logger.info("Saving user to database...");
            
            // Save user
            userService.saveUser(user);
            
            logger.info("User saved successfully!");
            return "redirect:/signin?success";
            
        } catch (Exception e) {
            logger.error("Error saving user: ", e);
            model.addAttribute("error", "An error occurred while creating your account: " + e.getMessage());
            return "signup";
        }
    }
    
    
    private String generateUsername(String firstName, String lastName) {
        if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
            return "user_" + System.currentTimeMillis();
        }
        String firstLetter = firstName.substring(0, 1);
        return (firstLetter + lastName).toLowerCase();
    }
}