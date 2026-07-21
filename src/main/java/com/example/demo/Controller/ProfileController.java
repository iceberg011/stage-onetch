package com.example.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.Entity.UserAccount;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProfileController {

    @GetMapping("/dashboard/profile")
    public String profile(HttpSession session, Model model) {
        System.out.println("=== GET /dashboard/profile called ===");
        
        UserAccount user = (UserAccount) session.getAttribute("user");
        
        if (user == null) {
            System.out.println("User not logged in, redirecting to signin");
            return "redirect:/signin";
        }
        
        // Set layout attributes
        model.addAttribute("pageTitle", "My Profile");
        model.addAttribute("pageContent", "UserProfile/AccountSettings");
        
        // User attributes
        model.addAttribute("user", user);
        model.addAttribute("userId", user.getId());
        model.addAttribute("username", user.getUsername() != null ? user.getUsername() : "N/A");
        model.addAttribute("email", user.getEmail() != null ? user.getEmail() : "N/A");
        model.addAttribute("firstName", user.getFirst_name() != null ? user.getFirst_name() : "User");
        model.addAttribute("lastName", user.getLast_name() != null ? user.getLast_name() : "");
        model.addAttribute("loginCount", user.getLogin_count() != null ? user.getLogin_count() : 0);
        model.addAttribute("isActive", user.getIs_active());
        model.addAttribute("isSuperuser", user.getIs_superuser());
        model.addAttribute("dateJoin", user.getDate_join() != null ? user.getDate_join() : "N/A");
        model.addAttribute("lastLogin", user.getLast_login() != null ? user.getLast_login() : "N/A");
        
        // Phone number
        Integer phoneNumber = user.gettele_phone();
        model.addAttribute("phoneNumber", phoneNumber != null ? phoneNumber : "Not provided");
        
        return "Components/layout";
    }
}