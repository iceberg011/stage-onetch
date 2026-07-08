package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;








import com.example.demo.Entity.UserAccount;
import com.example.demo.Service.UserService;

@Controller
public class SignupController {

    @Autowired
    private UserService userService;

    @GetMapping("/signup")
    public String showSignupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam(value = "first_name", required = false) String firstName,
            @RequestParam(value = "last_name", required = false) String lastName,
            Model model) {
        
        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "signup";
        }
        
        // Check if username already exists
        if (userService.userExists(username)) {
            model.addAttribute("error", "Username already exists");
            return "signup";
        }
        
        // Check if email already exists
        if (userService.userExistsByEmail(email)) {
            model.addAttribute("error", "Email already exists");
            return "signup";
        }
        
        // Create new user
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setFirst_name(firstName);
        user.setLast_name(lastName);
        
        // Save user (password will be encrypted in service)
        userService.saveUser(user);
        
        return "redirect:/signin?success";
    }
}