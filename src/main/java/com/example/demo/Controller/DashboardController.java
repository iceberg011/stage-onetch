package com.example.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.Entity.UserAccount;

import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        System.out.println("=== GET /dashboard called ===");
        
        // Get user from session
        UserAccount user = (UserAccount) session.getAttribute("user");
        
        if (user == null) {
            System.out.println("User not logged in, redirecting to signin");
            return "redirect:/signin";
        }
        
        System.out.println("User found in session: " + user.getUsername());
        System.out.println("First Name: " + user.getFirst_name());
        System.out.println("Last Name: " + user.getLast_name());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Login Count: " + user.getLogin_count());
        
        // Add all user attributes to the model
        model.addAttribute("user", user);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("firstName", user.getFirst_name());
        model.addAttribute("lastName", user.getLast_name());
        model.addAttribute("loginCount", user.getLogin_count());
        model.addAttribute("userId", user.getId());
        
        // Handle phone number safely
        Integer phoneNumber = user.gettele_phone();
        if (phoneNumber != null) {
            model.addAttribute("phoneNumber", phoneNumber);
        } else {
            model.addAttribute("phoneNumber", "Not provided");
        }
        
        return "dashboard";
    }
    
}