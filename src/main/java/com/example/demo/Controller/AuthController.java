package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.DTO.LoginRequest;
import com.example.demo.DTO.SignupRequest;
import com.example.demo.Entity.employees;
import com.example.demo.Service.EmployeeService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final EmployeeService employeeService;

    public AuthController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request, HttpServletRequest httpRequest) {
        try {
            // Check if passwords match
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                Map<String, String> response = new HashMap<>();
                response.put("success", "false");
                response.put("message", "Passwords do not match");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if username (nickname) exists
            if (employeeService.employeeExistsByNickname(request.getUsername())) {
                Map<String, String> response = new HashMap<>();
                response.put("success", "false");
                response.put("message", "Username already exists");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if email exists
            if (employeeService.employeeExistsByEmail(request.getEmail())) {
                Map<String, String> response = new HashMap<>();
                response.put("success", "false");
                response.put("message", "Email already exists");
                return ResponseEntity.badRequest().body(response);
            }

            // Register the employee - PASS BOTH parameters
            employees employee = employeeService.registerEmployee(request, httpRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Account created successfully");
            
            // Create user map properly
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", employee.getId());
            userMap.put("nickname", employee.getNickname());
            userMap.put("email", employee.getEmail());
            userMap.put("firstName", employee.getFirst_name());
            userMap.put("lastName", employee.getLast_name());
            userMap.put("mobile", employee.getMobile());
            userMap.put("empCode", employee.getEmp_code());
            userMap.put("isActive", employee.isIs_active());
            userMap.put("hireDate", employee.getHire_date());
            userMap.put("city", employee.getCity());
            
            response.put("employee", userMap);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("success", "false");
            response.put("message", "Error during signup: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody LoginRequest request, HttpSession session) {
        try {
            Optional<employees> employeeOpt = employeeService.authenticateEmployee(request);

            if (employeeOpt.isPresent()) {
                employees employee = employeeOpt.get();
                
                // Update login info
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

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Login successful");
                
                // Create user map properly
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", employee.getId());
                userMap.put("nickname", employee.getNickname());
                userMap.put("email", employee.getEmail());
                userMap.put("firstName", employee.getFirst_name());
                userMap.put("lastName", employee.getLast_name());
                userMap.put("mobile", employee.getMobile());
                userMap.put("empCode", employee.getEmp_code());
                userMap.put("isActive", employee.isIs_active());
                userMap.put("hireDate", employee.getHire_date());
                userMap.put("lastLogin", employee.getLast_login());
                userMap.put("departmentId", employee.getDepartment_id());
                userMap.put("positionId", employee.getPosition_id());
                userMap.put("appRole", employee.getApp_role());
                
                response.put("employee", userMap);

                return ResponseEntity.ok(response);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("success", "false");
                response.put("message", "Invalid credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("success", "false");
            response.put("message", "Error during signin: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signout(HttpSession session) {
        session.invalidate();
        Map<String, String> response = new HashMap<>();
        response.put("success", "true");
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean exists = employeeService.employeeExistsByEmail(email);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean exists = employeeService.employeeExistsByNickname(username);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/session")
    public ResponseEntity<?> getSession(HttpSession session) {
        employees employee = (employees) session.getAttribute("employee");
        if (employee == null) {
            Map<String, String> response = new HashMap<>();
            response.put("authenticated", "false");
            return ResponseEntity.ok(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", true);
        
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", employee.getId());
        userMap.put("nickname", employee.getNickname());
        userMap.put("email", employee.getEmail());
        userMap.put("firstName", employee.getFirst_name());
        userMap.put("lastName", employee.getLast_name());
        userMap.put("mobile", employee.getMobile());
        userMap.put("empCode", employee.getEmp_code());
        userMap.put("isActive", employee.isIs_active());
        userMap.put("hireDate", employee.getHire_date());
        userMap.put("lastLogin", employee.getLast_login());
        userMap.put("departmentId", employee.getDepartment_id());
        userMap.put("positionId", employee.getPosition_id());
        userMap.put("appRole", employee.getApp_role());
        
        response.put("employee", userMap);
        return ResponseEntity.ok(response);
    }
}