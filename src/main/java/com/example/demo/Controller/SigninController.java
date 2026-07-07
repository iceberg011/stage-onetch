package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Entity.UserAccount;
import com.example.demo.Service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class SigninController {

    @Autowired
    private UserService userService;

    @GetMapping("/signin")
    public String showSigninPage() {
        System.out.println("=== GET /signin called ===");
        return "signin";
    }

    @PostMapping("/signin")
    public String processSignin(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            Model model,
            HttpSession session) {
        
        System.out.println("=== POST /signin called ===");
        System.out.println("Email: " + email);
        
        try {
            // Check if userService is working
            if (userService == null) {
                System.err.println("UserService is NULL!");
                model.addAttribute("signinError", true);
                model.addAttribute("signinMessage", "Service error");
                return "signin";
            }
            
            // Validate credentials
            boolean isValid = userService.validateAuthenticationByEmail(email, password);
            System.out.println("Validation result: " + isValid);
            
            if (isValid) {
                UserAccount user = userService.getUserByEmail(email);
                
                if (user != null) {
                    session.setAttribute("user", user);
                    session.setAttribute("username", user.getUsername());
                    session.setAttribute("email", user.getEmail());
                    
                    System.out.println("Login successful for: " + email);
                    model.addAttribute("signinSuccess", true);
                    model.addAttribute("signinMessage", "Welcome back, " + user.getFirst_name() + "!");
                    
                    return "redirect:/dashboard";
                }
            }
            
            System.out.println("Login failed for: " + email);
            model.addAttribute("signinError", true);
            model.addAttribute("signinMessage", "Invalid email or password.");
            return "signin";
            
        } catch (Exception e) {
            System.err.println("Error during signin: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("signinError", true);
            model.addAttribute("signinMessage", "Error: " + e.getMessage());
            return "signin";
        }
    }
}