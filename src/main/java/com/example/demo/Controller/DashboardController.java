package com.example.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.Entity.employees;

import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        System.out.println("=== GET /dashboard called ===");
        
        employees employee = (employees) session.getAttribute("employee"); // Changed from "user" to "employee"
        
        if (employee == null) {
            System.out.println("Employee not logged in, redirecting to signin");
            return "redirect:/signin";
        }

        if (!DashboardAccessPolicy.canAccessDashboard(employee.getApp_role())) {
            System.out.println("Regular user blocked from dashboard: " + employee.getEmail());
            return "redirect:/";
        }
        
        System.out.println("Employee logged in: " + employee.getNickname() + " (" + employee.getEmail() + ")");
        
        // Set page title and active page (dashboard is default)
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("activePage", "DashComponent/dashboard");
        model.addAttribute("pageContent", "DashComponent/dashboard");
        
        // Set employee attributes
        model.addAttribute("employee", employee);
        model.addAttribute("employeeId", employee.getId());
        model.addAttribute("nickname", employee.getNickname() != null ? employee.getNickname() : "N/A");
        model.addAttribute("email", employee.getEmail() != null ? employee.getEmail() : "N/A");
        model.addAttribute("firstName", employee.getFirst_name() != null ? employee.getFirst_name() : "User");
        model.addAttribute("lastName", employee.getLast_name() != null ? employee.getLast_name() : "");
        model.addAttribute("empCode", employee.getEmp_code() != null ? employee.getEmp_code() : "N/A");
        model.addAttribute("isActive", employee.isIs_active());
        model.addAttribute("hireDate", employee.getHire_date() != null ? employee.getHire_date() : "N/A");
        
        // Set phone number (mobile)
        String mobile = employee.getMobile() != null ? employee.getMobile() : "Not provided";
        model.addAttribute("mobile", mobile);
        
        // Set role
        String role = getRoleName(employee.getApp_role());
        model.addAttribute("role", role);
        
        // Set department
        Integer departmentId = employee.getDepartment_id();
        model.addAttribute("departmentId", departmentId != null ? departmentId : "Not assigned");
        
        // Set position
        Integer positionId = employee.getPosition_id();
        model.addAttribute("positionId", positionId != null ? positionId : "Not assigned");
        
        // Return the layout template
        return "Components/layout";
    }
    
    @GetMapping("/dashboard/home")
    public String home(HttpSession session) {
        employees employee = (employees) session.getAttribute("employee");
        if (employee == null) {
            return "redirect:/signin";
        }
        if (!DashboardAccessPolicy.canAccessDashboard(employee.getApp_role())) {
            return "redirect:/";
        }
        return "redirect:/dashboard";
    }
    
    // Helper method to get role name from app_role
    private String getRoleName(Short appRole) {
        if (appRole == null) {
            return "User";
        }
        switch (appRole) {
            case 1:
                return "Administrator";
            case 2:
                return "Manager";
            case 3:
                return "Staff";
            default:
                return "User";
        }
    }
}