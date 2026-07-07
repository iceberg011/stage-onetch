package com.example.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        System.out.println("=== GET /dashboard called ===");
        
        // Check if user is logged in
        Object user = session.getAttribute("user");
        Object username = session.getAttribute("username");
        
        if (user == null) {
            System.out.println("User not logged in, redirecting to signin");
            return "redirect:/signin";
        }
        
        System.out.println("User is logged in: " + username);
        model.addAttribute("username", username);
        model.addAttribute("user", user);
        
        return "dashboard";
    }
}