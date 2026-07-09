package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.DTO.LoginRequest;
import com.example.demo.DTO.SignupRequest;
import com.example.demo.Entity.UserAccount;
import com.example.demo.Service.UserService;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            // Check if passwords match
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                Map<String, String> response = new HashMap<>();
                response.put("success", "false");
                response.put("message", "Passwords do not match");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if username exists
            if (userService.existsByUsername(request.getUsername())) {
                Map<String, String> response = new HashMap<>();
                response.put("success", "false");
                response.put("message", "Username already exists");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if email exists
            if (userService.existsByEmail(request.getEmail())) {
                Map<String, String> response = new HashMap<>();
                response.put("success", "false");
                response.put("message", "Email already exists");
                return ResponseEntity.badRequest().body(response);
            }

            // Register the user
            UserAccount user = userService.registerUser(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Account created successfully");
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "firstName", user.getFirst_name(),
                "lastName", user.getLast_name(),
                "phoneNumber", user.gettele_phone()
            ));

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
            Optional<UserAccount> userOpt = userService.authenticateUser(request);

            if (userOpt.isPresent()) {
                UserAccount user = userOpt.get();
                
                session.setAttribute("user", user);
                session.setAttribute("username", user.getUsername());
                session.setAttribute("email", user.getEmail());

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Login successful");
                response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "firstName", user.getFirst_name(),
                    "lastName", user.getLast_name(),
                    "phoneNumber", user.gettele_phone()
                ));

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
        boolean exists = userService.existsByEmail(email);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
}