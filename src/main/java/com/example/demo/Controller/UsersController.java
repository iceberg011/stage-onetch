package com.example.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.Entity.UserAccount;
import com.example.demo.Repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.example.demo.Service.UserService;
import java.util.Optional;




@Controller
public class UsersController {

    private final UserRepository userRepository;
    private final UserService userService;

    public UsersController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    // ===== LIST ALL USERS WITH SEARCH & FILTER =====
    @GetMapping("/dashboard/Users")
    public String listUsers(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "field", required = false) String field,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "status", required = false) String status,
            HttpSession session,
            Model model) {
        
        System.out.println("=== GET /dashboard/Users called ===");
        System.out.println("Search: " + search);
        System.out.println("Field: " + field);
        System.out.println("Sort: " + sort);
        System.out.println("Role: " + role);
        System.out.println("Status: " + status);
        
        UserAccount currentUser = (UserAccount) session.getAttribute("user");
        
        if (currentUser == null) {
            System.out.println("User not logged in, redirecting to signin");
            return "redirect:/signin";
        }
        
        // ===== CALL THE SEARCH FUNCTION =====
        List<UserAccount> filteredUsers = userService.searchUsers(search, field, sort, role, status);
        
        // Calculate statistics
        long totalUsers = filteredUsers.size();
        long activeUsers = 0;
        long inactiveUsers = 0;
        long adminCount = 0;
        
        for (UserAccount user : filteredUsers) {
            if (user.getIs_active()) {
                activeUsers++;
            } else {
                inactiveUsers++;
            }
            if (user.getIs_superuser()) {
                adminCount++;
            }
        }
        
        // Set layout attributes
        model.addAttribute("pageTitle", "Users List");
        model.addAttribute("pageContent", "User/Users");
        
        // User info for sidebar
        model.addAttribute("user", currentUser);
        model.addAttribute("firstName", currentUser.getFirst_name());
        model.addAttribute("lastName", currentUser.getLast_name());
        model.addAttribute("email", currentUser.getEmail());
        model.addAttribute("username", currentUser.getUsername());
        
        // User statistics
        model.addAttribute("userCount", totalUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("inactiveUsers", inactiveUsers);
        model.addAttribute("adminCount", adminCount);
        
        // ALL users for the table
        model.addAttribute("users", filteredUsers);
        
        // Preserve filter values for the form
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedField", field);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);
        
        return "Components/layout";
    }














    
    // ===== EDIT USER - SHOW FORM =====
    @GetMapping("/dashboard/users/edit/{id}")
    public String editUser(@PathVariable Long id, HttpSession session, Model model) {
        System.out.println("=== GET /dashboard/users/edit/" + id + " called ===");
        
        UserAccount currentUser = (UserAccount) session.getAttribute("user");
        
        if (currentUser == null) {
            return "redirect:/signin";
        }
        
        // Find user by ID
        Optional<UserAccount> userOpt = userRepository.findById(id);
        
        if (userOpt.isEmpty()) {
            System.out.println("User not found with ID: " + id);
            return "redirect:/dashboard/Users?error=UserNotFound";
        }
        
        UserAccount userToEdit = userOpt.get();
        
        // Set layout attributes
        model.addAttribute("pageTitle", "Edit User");
        model.addAttribute("pageContent", "User/EditUser");
        
        // User info for sidebar
        model.addAttribute("firstName", currentUser.getFirst_name());
        model.addAttribute("lastName", currentUser.getLast_name());
        model.addAttribute("email", currentUser.getEmail());
        model.addAttribute("username", currentUser.getUsername());
        
        // User to edit
        model.addAttribute("user", userToEdit);
        
        return "Components/layout";
    }

    // ===== UPDATE USER =====
    @PostMapping("/dashboard/users/update/{id}")
    public String updateUser(
            @PathVariable Long id,
            @RequestParam("first_name") String firstName,
            @RequestParam("last_name") String lastName,
            @RequestParam("email") String email,
            @RequestParam(value = "tele_phone", required = false) String telePhone,
            @RequestParam("username") String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "confirm_password", required = false) String confirmPassword,
            @RequestParam(value = "role", defaultValue = "user") String role,
            @RequestParam(value = "is_active", defaultValue = "true") String isActive,
            @RequestParam(value = "is_staff", defaultValue = "false") boolean isStaff,
            @RequestParam(value = "is_superuser", defaultValue = "false") boolean isSuperuser,
            @RequestParam(value = "is_public", defaultValue = "false") boolean isPublic,
            @RequestParam(value = "can_manage_all_dept", defaultValue = "false") boolean canManageAllDept,
            HttpSession session,
            Model model) {

        System.out.println("=== POST /dashboard/users/update/" + id + " called ===");
        
        UserAccount currentUser = (UserAccount) session.getAttribute("user");
        
        if (currentUser == null) {
            return "redirect:/signin";
        }
        
        // Find user by ID
        Optional<UserAccount> userOpt = userRepository.findById(id);
        
        if (userOpt.isEmpty()) {
            System.out.println("User not found with ID: " + id);
            return "redirect:/dashboard/Users?error=UserNotFound";
        }
        
        UserAccount userToUpdate = userOpt.get();
        
        // Check if password is being changed
        if (password != null && !password.isEmpty()) {
            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "Passwords do not match");
                model.addAttribute("user", userToUpdate);
                model.addAttribute("pageTitle", "Edit User");
                model.addAttribute("pageContent", "User/EditUser");
                model.addAttribute("firstName", currentUser.getFirst_name());
                model.addAttribute("lastName", currentUser.getLast_name());
                model.addAttribute("email", currentUser.getEmail());
                model.addAttribute("username", currentUser.getUsername());
                return "Components/layout";
            }
            // Set new password (will be encrypted in service)
            userToUpdate.setPassword(password);
        }
        
        // Update user fields
        userToUpdate.setFirst_name(firstName);
        userToUpdate.setLast_name(lastName);
        userToUpdate.setEmail(email);
        userToUpdate.setUsername(username);
        userToUpdate.setIs_active("true".equalsIgnoreCase(isActive));
        userToUpdate.setIs_staff(isStaff);
        userToUpdate.setIs_superuser(isSuperuser);
        userToUpdate.setIs_public(isPublic);
        userToUpdate.setCan_manage_all_dept(canManageAllDept);
        userToUpdate.setUpdate_time(LocalDateTime.now());
        
        // Update phone number
        if (telePhone != null && !telePhone.isEmpty()) {
            try {
                userToUpdate.settele_phone(Integer.parseInt(telePhone));
            } catch (NumberFormatException e) {
                userToUpdate.settele_phone(null);
            }
        } else {
            userToUpdate.settele_phone(null);
        }
        
        try {
            // Save user
            userService.saveUser(userToUpdate);
            return "redirect:/dashboard/Users?success=UserUpdated";
        } catch (Exception e) {
            model.addAttribute("error", "Error updating user: " + e.getMessage());
            model.addAttribute("user", userToUpdate);
            model.addAttribute("pageTitle", "Edit User");
            model.addAttribute("pageContent", "User/EditUser");
            model.addAttribute("firstName", currentUser.getFirst_name());
            model.addAttribute("lastName", currentUser.getLast_name());
            model.addAttribute("email", currentUser.getEmail());
            model.addAttribute("username", currentUser.getUsername());
            return "Components/layout";
        }
    }

    // ===== DELETE USER =====
    @GetMapping("/dashboard/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session) {
        System.out.println("=== GET /dashboard/users/delete/" + id + " called ===");
        
        UserAccount currentUser = (UserAccount) session.getAttribute("user");
        
        if (currentUser == null) {
            return "redirect:/signin";
        }
        
        // Don't allow deleting yourself
        if (currentUser.getId().equals(id)) {
            return "redirect:/dashboard/Users?error=CannotDeleteSelf";
        }
        
        Optional<UserAccount> userOpt = userRepository.findById(id);
        
        if (userOpt.isPresent()) {
            userRepository.deleteById(id);
            System.out.println("User deleted with ID: " + id);
            return "redirect:/dashboard/Users?success=UserDeleted";
        }
        
        return "redirect:/dashboard/Users?error=UserNotFound";
    }

    @GetMapping("/api/users")
    @ResponseBody
    public List<UserAccount> getAllUsersJson() {
        return userRepository.findAll();
    }


























    

    @GetMapping("/dashboard/users/CreateUser")
    public String showNewUserPage(HttpSession session, Model model) {
        System.out.println("=== GET /dashboard/users/CreateUser called ===");
        
        UserAccount currentUser = (UserAccount) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/signin";
        }
        
        model.addAttribute("pageTitle", "Add New User");
        model.addAttribute("pageContent", "User/Newuser");
        model.addAttribute("firstName", currentUser.getFirst_name());
        model.addAttribute("lastName", currentUser.getLast_name());
        model.addAttribute("email", currentUser.getEmail());
        
        return "Components/layout";
    }

    @PostMapping("/dashboard/users/CreateUser")
    public String addUser(
            @RequestParam("first_name") String firstName,
            @RequestParam("last_name") String lastName,
            @RequestParam("email") String email,
            @RequestParam(value = "tele_phone", required = false) String telePhone,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam("password") String password,
            @RequestParam("confirm_password") String confirmPassword,
            @RequestParam(value = "role", defaultValue = "user") String role,
            @RequestParam(value = "is_active", defaultValue = "true") String isActive,
            @RequestParam(value = "is_staff", defaultValue = "false") boolean isStaff,
            @RequestParam(value = "is_superuser", defaultValue = "false") boolean isSuperuser,
            @RequestParam(value = "is_public", defaultValue = "false") boolean isPublic,
            @RequestParam(value = "can_manage_all_dept", defaultValue = "false") boolean canManageAllDept,
            HttpSession session,
            Model model) {

        UserAccount currentUser = (UserAccount) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/signin";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("pageTitle", "Add New User");
            model.addAttribute("pageContent", "User/Newuser");
            return "Components/layout";
        }

        if (userService.userExistsByEmail(email)) {
            model.addAttribute("error", "Email already exists");
            model.addAttribute("pageTitle", "Add New User");
            model.addAttribute("pageContent", "User/Newuser");
            return "Components/layout";
        }

        if (username == null || username.isEmpty()) {
            username = generateUsername(firstName, lastName);
        }

        if (userService.userExists(username)) {
            int counter = 1;
            String baseUsername = username;
            while (userService.userExists(username)) {
                username = baseUsername + counter;
                counter++;
            }
        }

        UserAccount newUser = new UserAccount();
        newUser.setFirst_name(firstName);
        newUser.setLast_name(lastName);
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setDate_join(LocalDate.now());
        newUser.setUpdate_time(LocalDateTime.now());
        newUser.setLogin_count(0);
        newUser.setLogin_ip(0);
        newUser.setLogin_id(0);
        newUser.setLogin_type("email");
        newUser.setIs_active("true".equalsIgnoreCase(isActive));
        newUser.setIs_staff(isStaff);
        newUser.setIs_superuser(isSuperuser);
        newUser.setIs_public(isPublic);
        newUser.setCan_manage_all_dept(canManageAllDept);
        newUser.setDel_flag(0);
        newUser.setPhoto("/static/user/default.png");

        if (telePhone != null && !telePhone.isEmpty()) {
            try {
                newUser.settele_phone(Integer.parseInt(telePhone));
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        try {
            userService.saveUser(newUser);
            return "redirect:/dashboard/Users";
        } catch (Exception e) {
            model.addAttribute("error", "Error creating user: " + e.getMessage());
            model.addAttribute("pageTitle", "Add New User");
            model.addAttribute("pageContent", "User/Newuser");
            return "Components/layout";
        }
    }

    private String generateUsername(String firstName, String lastName) {
        if (firstName == null || firstName.isEmpty()) {
            firstName = "user";
        }
        if (lastName == null || lastName.isEmpty()) {
            lastName = "unknown";
        }
        String firstLetter = firstName.substring(0, 1);
        return (firstLetter + lastName).toLowerCase().replaceAll("\\s+", "");
    }












}










    
