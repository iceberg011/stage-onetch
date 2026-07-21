package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.Entity.UserAccount;
import com.example.demo.Service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class LogoutController {

    @Autowired
    private UserService userService;

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("=== Logout called ===");
        
        // Get user from session
        UserAccount user = (UserAccount) session.getAttribute("user");
        
        // Clear session_key from database
        if (user != null) {
            userService.clearSessionKey(user.getId());
            System.out.println("Session key cleared from database for user: " + user.getUsername());
        }
        
        // Clear remember me cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("remember_token".equals(cookie.getName())) {
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                    System.out.println("Remember me cookie cleared");
                    break;
                }
            }
        }
        
        // Invalidate session
        session.invalidate();
        System.out.println("Session invalidated");
        
        return "redirect:/signin?logout=true";
    }
}