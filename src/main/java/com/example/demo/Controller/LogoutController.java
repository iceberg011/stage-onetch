package com.example.demo.Controller;

import com.example.demo.Entity.employees;
import com.example.demo.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class LogoutController {

    @Autowired
    private EmployeeService employeeService; // Changed from UserService to EmployeeService

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("=== Logout called ===");
        
        // Get employee from session
        employees employee = (employees) session.getAttribute("employee");
        
        // Clear session_key from database
        if (employee != null) {
            employee.clearRememberToken();
            employeeService.saveEmployee(employee);
            System.out.println("Session key cleared from database for employee: " + employee.getNickname());
        }
        
        // Clear remember me cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("remember_token".equals(cookie.getName()) || "remember-me".equals(cookie.getName())) {
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                    System.out.println("Remember me cookie cleared: " + cookie.getName());
                    break;
                }
            }
        }
        
        // Clear session attributes
        session.removeAttribute("employee");
        session.removeAttribute("employeeId");
        session.removeAttribute("nickname");
        session.removeAttribute("email");
        session.removeAttribute("firstName");
        session.removeAttribute("lastName");
        session.removeAttribute("mobile");
        session.removeAttribute("empCode");
        session.removeAttribute("isActive");
        session.removeAttribute("hireDate");
        session.removeAttribute("lastLogin");
        session.removeAttribute("photo");
        session.removeAttribute("departmentId");
        session.removeAttribute("positionId");
        session.removeAttribute("appRole");
        
        // Invalidate session
        session.invalidate();
        System.out.println("Session invalidated");
        
        return "redirect:/signin?logout=true";
    }

    @GetMapping("/signout")
    public String signout(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("=== Signout called ===");
        
        // Get employee from session
        employees employee = (employees) session.getAttribute("employee");
        
        // Clear session_key from database
        if (employee != null) {
            employee.clearRememberToken();
            employeeService.saveEmployee(employee);
            System.out.println("Session key cleared from database for employee: " + employee.getNickname());
        }
        
        // Clear remember me cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("remember_token".equals(cookie.getName()) || "remember-me".equals(cookie.getName())) {
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                    System.out.println("Remember me cookie cleared: " + cookie.getName());
                    break;
                }
            }
        }
        
        // Clear session attributes
        session.removeAttribute("employee");
        session.removeAttribute("employeeId");
        session.removeAttribute("nickname");
        session.removeAttribute("email");
        session.removeAttribute("firstName");
        session.removeAttribute("lastName");
        session.removeAttribute("mobile");
        session.removeAttribute("empCode");
        session.removeAttribute("isActive");
        session.removeAttribute("hireDate");
        session.removeAttribute("lastLogin");
        session.removeAttribute("photo");
        session.removeAttribute("departmentId");
        session.removeAttribute("positionId");
        session.removeAttribute("appRole");
        
        // Invalidate session
        session.invalidate();
        System.out.println("Session invalidated");
        
        return "redirect:/signin?logout=true";
    }
}