package com.example.demo.Controller;

import com.example.demo.DTO.SignupRequest;
import com.example.demo.Entity.employees;
import com.example.demo.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class SignupController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/signup")
    public String showSignupPage(Model model) {
        System.out.println("=== GET /signup called ===");
        model.addAttribute("pageTitle", "Sign Up - OneTech");
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpServletRequest request,
            Model model,
            HttpSession session) {

        System.out.println("=== POST /signup called ===");
        System.out.println("FirstName: " + firstName);
        System.out.println("LastName: " + lastName);
        System.out.println("Email: " + email);
        System.out.println("Phone: " + phoneNumber);
        System.out.println("Username: " + username);
        
        // Validate required fields
        if (firstName == null || firstName.trim().isEmpty()) {
            model.addAttribute("error", "First name is required");
            model.addAttribute("pageTitle", "Sign Up - OneTech");
            return "signup";
        }
        
        if (lastName == null || lastName.trim().isEmpty()) {
            model.addAttribute("error", "Last name is required");
            model.addAttribute("pageTitle", "Sign Up - OneTech");
            return "signup";
        }
        
        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Email is required");
            model.addAttribute("pageTitle", "Sign Up - OneTech");
            return "signup";
        }
        
        // Validate passwords
        if (password == null || password.isEmpty()) {
            model.addAttribute("error", "Password is required");
            model.addAttribute("pageTitle", "Sign Up - OneTech");
            return "signup";
        }
        
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("pageTitle", "Sign Up - OneTech");
            return "signup";
        }
        
        if (password.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters long");
            model.addAttribute("pageTitle", "Sign Up - OneTech");
            return "signup";
        }
        
        try {
            // Check if email exists
            if (employeeService.employeeExistsByEmail(email)) {
                model.addAttribute("error", "Email already exists");
                model.addAttribute("pageTitle", "Sign Up - OneTech");
                return "signup";
            }
            
            // Create signup request
            SignupRequest signupRequest = new SignupRequest();
            signupRequest.setFirstName(firstName.trim());
            signupRequest.setLastName(lastName.trim());
            signupRequest.setEmail(email.trim().toLowerCase());
            signupRequest.setPhoneNumber(phoneNumber != null ? phoneNumber.trim() : "");
            signupRequest.setUsername(username != null && !username.isEmpty() ? username.trim() : "");
            signupRequest.setPassword(password);
            signupRequest.setConfirmPassword(confirmPassword);
            // Register employee - passes HttpServletRequest for location detection
            employees newEmployee = employeeService.registerEmployee(signupRequest, request);
            
            System.out.println("Employee registered successfully with ID: " + newEmployee.getId());
            System.out.println("Nickname: " + newEmployee.getNickname());
            System.out.println("Email: " + newEmployee.getEmail());
            System.out.println("City (auto-detected): " + newEmployee.getCity());
            
            // Auto-login after registration
            session.setAttribute("employee", newEmployee);
            session.setAttribute("employeeId", newEmployee.getId());
            session.setAttribute("nickname", newEmployee.getNickname());
            session.setAttribute("email", newEmployee.getEmail());
            session.setAttribute("firstName", newEmployee.getFirst_name());
            session.setAttribute("lastName", newEmployee.getLast_name());
            session.setAttribute("mobile", newEmployee.getMobile());
            session.setAttribute("empCode", newEmployee.getEmp_code());
            session.setAttribute("city", newEmployee.getCity());
            
            return "redirect:/dashboard/Employees?success=AccountCreated";
            
        } catch (Exception e) {
            System.err.println("Error during signup: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "An error occurred during sign up: " + e.getMessage());
            model.addAttribute("pageTitle", "Sign Up - OneTech");
            return "signup";
        }
    }
}