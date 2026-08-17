package com.example.demo.Controller;

import com.example.demo.Entity.employees;
import com.example.demo.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
public class SigninController {

    @Autowired
    private EmployeeService employeeService;

    private static final int REMEMBER_ME_EXPIRY_HOURS = 24;
    private static final String REMEMBER_ME_COOKIE_NAME = "remember_token";
    
    private static final Logger logger = LoggerFactory.getLogger(SigninController.class);

    @GetMapping("/signin")
    public String showSigninPage(
            @RequestParam(value = "success", required = false) String success,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "error", required = false) String error,
            @CookieValue(value = REMEMBER_ME_COOKIE_NAME, required = false) String rememberToken,
            HttpSession session,
            HttpServletResponse response,
            Model model) {
        
        System.out.println("=== GET /signin called ===");
        
        // Check if there's a valid remember me token for auto-login
        if (rememberToken != null && !rememberToken.isEmpty()) {
            Optional<employees> employeeOpt = employeeService.findBySessionKey(rememberToken);
            if (employeeOpt.isPresent()) {
                employees employee = employeeOpt.get();
                if (employee.isRememberTokenValid()) {
                    session.setAttribute("employee", employee);
                    session.setAttribute("employeeId", employee.getId());
                    session.setAttribute("nickname", employee.getNickname());
                    session.setAttribute("email", employee.getEmail());
                    session.setAttribute("firstName", employee.getFirst_name());
                    session.setAttribute("lastName", employee.getLast_name());
                    session.setAttribute("mobile", employee.getMobile());
                    session.setAttribute("empCode", employee.getEmp_code());
                    session.setAttribute("appRole", employee.getApp_role());
                    session.setAttribute("isActive", employee.isIs_active());
                    
                    System.out.println("Auto-login successful for: " + employee.getEmail());
                    
                    employee.setLast_login(LocalDateTime.now());
                    employeeService.saveEmployee(employee);
                    
                    if (DashboardAccessPolicy.canAccessDashboard(employee.getApp_role())) {
                        return "redirect:/dashboard";
                    }
                    return "redirect:/";
                } else {
                    clearRememberMeCookie(response);
                    employee.clearRememberToken();
                    employeeService.saveEmployee(employee);
                    System.out.println("Invalid or expired remember token, cleared");
                }
            } else {
                clearRememberMeCookie(response);
                System.out.println("Remember token doesn't match any employee, cleared");
            }
        }
        
        // Check if user is already logged in
        employees currentEmployee = (employees) session.getAttribute("employee");
        if (currentEmployee != null) {
            System.out.println("User already logged in: " + currentEmployee.getEmail());
            if (DashboardAccessPolicy.canAccessDashboard(currentEmployee.getApp_role())) {
                return "redirect:/dashboard";
            }
            return "redirect:/";
        }
        
        if (success != null) {
            model.addAttribute("signinSuccess", true);
            model.addAttribute("signinMessage", "Account created successfully! Please sign in.");
        }
        
        if (logout != null) {
            model.addAttribute("signinSuccess", true);
            model.addAttribute("signinMessage", "You have been logged out successfully.");
        }
        
        if (error != null) {
            model.addAttribute("signinError", true);
            model.addAttribute("signinMessage", "Invalid email or password. Please try again.");
        }
        
        model.addAttribute("pageTitle", "Sign In - OneTech");
        return "signin";
    }

    @PostMapping("/signin")
public String processSignin(
        @RequestParam("username") String username,
        @RequestParam("password") String password,
        @RequestParam(value = "remember-me", required = false) String rememberMe,
        Model model,
        HttpSession session,
        HttpServletResponse response) {
    
    System.out.println("=== POST /signin called ===");
    System.out.println("Username/Email: " + username);
    System.out.println("Password length: " + (password != null ? password.length() : 0));
    System.out.println("Remember Me: " + (rememberMe != null ? "YES" : "NO"));
    
    try {
        // Try to find employee by email, nickname, or emp_code
        Optional<employees> employeeOpt = employeeService.findByUsernameOrEmailOrEmpCode(username);
        
        if (employeeOpt.isPresent()) {
            employees employee = employeeOpt.get();
            System.out.println("✅ Employee found!");
            System.out.println("   ID: " + employee.getId());
            System.out.println("   Nickname: " + employee.getNickname());
            System.out.println("   Email: " + employee.getEmail());
            System.out.println("   Emp Code: " + employee.getEmp_code());
            System.out.println("   Is Active: " + employee.isIs_active());
            System.out.println("   Has Password: " + (employee.getSelf_password() != null ? "YES" : "NO"));
            
            // Check if account is active
            if (!employee.isIs_active()) {
                System.out.println("❌ Account is inactive");
                model.addAttribute("signinError", true);
                model.addAttribute("signinMessage", "Your account is inactive. Please contact support.");
                model.addAttribute("pageTitle", "Sign In - OneTech");
                return "signin";
            }
            
            // Validate password
            boolean isValid = false;
            
            // Check if password matches self_password
            if (employee.getSelf_password() != null) {
                isValid = employeeService.validatePassword(employee, password);
                System.out.println("Password validation result: " + isValid);
            } else {
                System.out.println("⚠️ Employee has no password set!");
            }
            
            // Check if password matches emp_code (for convenience)
            if (!isValid && employee.getEmp_code() != null && employee.getEmp_code().equals(password)) {
                isValid = true;
                System.out.println("✅ Password matched emp_code");
            }
            
            System.out.println("Final validation result: " + isValid);
            
            if (isValid && DashboardAccessPolicy.canAccessDashboard(employee.getApp_role())) {
                // Update login information
                employee.setLast_login(LocalDateTime.now());
                employeeService.saveEmployee(employee);
                
                // Set session attributes
                session.setAttribute("employee", employee);
                session.setAttribute("employeeId", employee.getId());
                session.setAttribute("nickname", employee.getNickname());
                session.setAttribute("email", employee.getEmail());
                session.setAttribute("firstName", employee.getFirst_name());
                session.setAttribute("lastName", employee.getLast_name());
                session.setAttribute("mobile", employee.getMobile());
                session.setAttribute("empCode", employee.getEmp_code());
                session.setAttribute("isActive", employee.isIs_active());
                session.setAttribute("hireDate", employee.getHire_date());
                session.setAttribute("lastLogin", employee.getLast_login());
                session.setAttribute("photo", employee.getPhoto());
                session.setAttribute("departmentId", employee.getDepartment_id());
                session.setAttribute("positionId", employee.getPosition_id());
                session.setAttribute("appRole", employee.getApp_role());
                
                System.out.println("✅ Dashboard login successful for: " + username);
                
                // Handle remember me
                if (rememberMe != null && rememberMe.equals("on")) {
                    String token = generateRememberMeToken();
                    String sessionKey = token + "_" + System.currentTimeMillis();
                    
                    employee.setSession_key(sessionKey);
                    employeeService.saveEmployee(employee);
                    System.out.println("Session key saved: " + sessionKey);
                    
                    setRememberMeCookie(response, sessionKey);
                    System.out.println("Remember me cookie created");
                } else {
                    employee.clearRememberToken();
                    employeeService.saveEmployee(employee);
                    clearRememberMeCookie(response);
                }
                
                return "redirect:/dashboard";
            } else if (isValid && !DashboardAccessPolicy.canAccessDashboard(employee.getApp_role())) {
                employee.setLast_login(LocalDateTime.now());
                employeeService.saveEmployee(employee);
                session.setAttribute("employee", employee);
                session.setAttribute("employeeId", employee.getId());
                session.setAttribute("nickname", employee.getNickname());
                session.setAttribute("email", employee.getEmail());
                session.setAttribute("firstName", employee.getFirst_name());
                session.setAttribute("lastName", employee.getLast_name());
                session.setAttribute("mobile", employee.getMobile());
                session.setAttribute("empCode", employee.getEmp_code());
                session.setAttribute("appRole", employee.getApp_role());
                session.setAttribute("isActive", employee.isIs_active());

                if (rememberMe != null && rememberMe.equals("on")) {
                    String token = generateRememberMeToken();
                    String sessionKey = token + "_" + System.currentTimeMillis();
                    employee.setSession_key(sessionKey);
                    employeeService.saveEmployee(employee);
                    setRememberMeCookie(response, sessionKey);
                    System.out.println("Remember me enabled for regular user: " + employee.getEmail());
                } else {
                    employee.clearRememberToken();
                    employeeService.saveEmployee(employee);
                    clearRememberMeCookie(response);
                }

                System.out.println("✅ Front-office login successful for regular user: " + username);
                return "redirect:/";
            } else {
                System.out.println("❌ Password validation failed");
                return "redirect:/signin?error=true";
            }
        } else {
            System.out.println("❌ Employee not found for: " + username);
        }
        
        System.out.println("❌ Login failed for: " + username);
        model.addAttribute("signinError", true);
        model.addAttribute("signinMessage", "Invalid username/email or password.");
        model.addAttribute("pageTitle", "Sign In - OneTech");
        return "signin";
        
    } catch (Exception e) {
        System.err.println("❌ Error during signin: " + e.getMessage());
        e.printStackTrace();
        model.addAttribute("signinError", true);
        model.addAttribute("signinMessage", "Error: " + e.getMessage());
        model.addAttribute("pageTitle", "Sign In - OneTech");
        return "signin";
    }
}
    // ===== HELPER METHODS =====
    
    private String generateRememberMeToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void setRememberMeCookie(HttpServletResponse response, String sessionKey) {
        Cookie cookie = new Cookie(REMEMBER_ME_COOKIE_NAME, sessionKey);
        cookie.setMaxAge(60 * 60 * REMEMBER_ME_EXPIRY_HOURS);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);

        logger.info("=== Token ATTEMPT ===");
        logger.info("rememberme: {}", REMEMBER_ME_COOKIE_NAME);
        logger.info("token: {}", sessionKey);

        response.addCookie(cookie);
        System.out.println("Remember me cookie set - expires in " + REMEMBER_ME_EXPIRY_HOURS + " hours");
    }

    private void clearRememberMeCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REMEMBER_ME_COOKIE_NAME, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        System.out.println("Remember me cookie cleared");
    }

}