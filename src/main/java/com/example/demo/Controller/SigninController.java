package com.example.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SigninController {

    @GetMapping("/signin")
    public String showSigninPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "success", required = false) String success,
            Model model) {
        
        System.out.println("=== GET /signin called ===");
        System.out.println("Error param: " + error);
        System.out.println("Logout param: " + logout);
        System.out.println("Success param: " + success);
        
        if (error != null) {
            model.addAttribute("signinError", true);
            model.addAttribute("signinMessage", "Invalid email or password.");
        }
        
        if (logout != null) {
            model.addAttribute("signinSuccess", true);
            model.addAttribute("signinMessage", "You have been logged out successfully.");
        }
        
        if (success != null) {
            model.addAttribute("signinSuccess", true);
            model.addAttribute("signinMessage", "Account created successfully! Please sign in.");
        }
        
        return "signin";
    }
}