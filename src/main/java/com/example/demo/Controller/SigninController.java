package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Entity.UserAccount;
import com.example.demo.Service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

@Controller
public class SigninController {

    @Autowired
    private UserService userService;

    private static final int REMEMBER_ME_EXPIRY_HOURS = 24;
    private static final String REMEMBER_ME_COOKIE_NAME = "remember_token";
    
    
    private static final Logger logger = LoggerFactory.getLogger(SignupController.class);


    @GetMapping("/signin")
    public String showSigninPage(
            @RequestParam(value = "success", required = false) String success,
            @RequestParam(value = "logout", required = false) String logout,
            @CookieValue(value = REMEMBER_ME_COOKIE_NAME, required = false) String rememberToken,
            HttpSession session,
            HttpServletResponse response,
            Model model) {
        
        System.out.println("=== GET /signin called ===");
        
        // Check if there's a valid remember me token for auto-login
        if (rememberToken != null && !rememberToken.isEmpty()) {
            UserAccount user = userService.findByRememberToken(rememberToken);
            if (user != null && user.isRememberTokenValid()) {
                // Auto-login the user
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("email", user.getEmail());
                session.setAttribute("firstName", user.getFirst_name());
                session.setAttribute("lastName", user.getLast_name());
                session.setAttribute("loginCount", user.getLogin_count());
                
                System.out.println("Auto-login successful for: " + user.getEmail());
                
                // Update login info
                userService.updateUserLoginInfo(user);
                
                return "redirect:/DashComponent/dashboard";
            } else {
                // Invalid or expired token, clear it
                clearRememberMeCookie(response);
                if (user != null) {
                    userService.clearSessionKey(user.getId());
                }
                System.out.println("Invalid or expired remember token, cleared");
            }
        }
        
        if (success != null) {
            model.addAttribute("signinSuccess", true);
            model.addAttribute("signinMessage", "Account created successfully! Please sign in.");
        }
        
        if (logout != null) {
            model.addAttribute("signinSuccess", true);
            model.addAttribute("signinMessage", "You have been logged out successfully.");
        }
        
        return "signin";
    }

    @PostMapping("/signin")
    public String processSignin(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "remember-me", required = false) String rememberMe,
            Model model,
            HttpSession session,
            HttpServletResponse response) {
        
        System.out.println("=== POST /signin called ===");
        System.out.println("Email: " + email);
        System.out.println("Remember Me: " + (rememberMe != null ? "YES" : "NO"));
        
        try {
            // Check if email exists
            if (!userService.userExistsByEmail(email)) {
                System.out.println("Email not found: " + email);
                model.addAttribute("signinError", true);
                model.addAttribute("signinMessage", "Invalid email or password.");
                return "signin";
            }
            
            // Get user by email
            UserAccount user = userService.getUserByEmail(email);
            
            if (user != null) {
                System.out.println("User found: " + user.getUsername());
                
                // Validate password
                boolean isValid = userService.validateAuthenticationByEmail(email, password);
                System.out.println("Password validation result: " + isValid);
                
                if (isValid) {
                    // Update login information
                    userService.updateUserLoginInfo(user);
                    
                    // Set session attributes
                    session.setAttribute("user", user);
                    session.setAttribute("userId", user.getId());
                    session.setAttribute("username", user.getUsername());
                    session.setAttribute("email", user.getEmail());
                    session.setAttribute("firstName", user.getFirst_name());
                    session.setAttribute("lastName", user.getLast_name());
                    session.setAttribute("loginCount", user.getLogin_count());
                    
                    System.out.println("Login successful for: " + email);
                    
                    // ===== HANDLE REMEMBER ME =====
                    if (rememberMe != null && rememberMe.equals("on")) {
                        // Generate unique token with timestamp
                        String token = generateRememberMeToken();
                        String session_key = token + "_" + System.currentTimeMillis();
                        
                        // Save token in session_key field
                        userService.saveSessionKey(user.getId(), session_key);
                        System.out.println("Session key saved in database for user: " + email);
                        System.out.println("Session key: " + session_key);
                        
                        // Create and add cookie with just the token
                        setRememberMeCookie(response, token);
                        System.out.println("Remember me cookie created for: " + email);
                    } else {
                        // Clear any existing remember me token if not checked
                        userService.clearSessionKey(user.getId());
                        clearRememberMeCookie(response);
                    }
                    
                    return "redirect:/DashComponent/dashboard";
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

   
    private String generateRememberMeToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

 
    private void setRememberMeCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(REMEMBER_ME_COOKIE_NAME, token);
        cookie.setMaxAge(60 * 60 * REMEMBER_ME_EXPIRY_HOURS);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        // cookie.setSecure(true); // Uncomment in production with HTTPS

        logger.info("=== Token ATTEMPT ===");
        logger.info("rememberme: {}", REMEMBER_ME_COOKIE_NAME);
        logger.info("token: {}", token);


        response.addCookie(cookie);
        System.out.println("Remember me cookie set - expires in " + REMEMBER_ME_EXPIRY_HOURS + " hours");
    }

    /**
     * Clear the remember me cookie
     */
    private void clearRememberMeCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REMEMBER_ME_COOKIE_NAME, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        System.out.println("Remember me cookie cleared");
    }
}