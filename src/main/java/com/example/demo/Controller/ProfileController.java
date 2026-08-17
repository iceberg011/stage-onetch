package com.example.demo.Controller;

import com.example.demo.Entity.employees;
import com.example.demo.Repository.EmployeesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class ProfileController {

    @Autowired
    private EmployeesRepository employeesRepository;

    @GetMapping("/user/profile/{id}") // Changed from /dashboard/profile/{id}
    public String viewProfile(@PathVariable Long id, HttpSession session, Model model) {
        System.out.println("=== GET /user/profile/" + id + " called ===");
        
        employees currentEmployee = (employees) session.getAttribute("employee");
        
        if (currentEmployee == null) {
            return "redirect:/signin";
        }
        
        Optional<employees> employeeOpt = employeesRepository.findById(id);
        
        if (employeeOpt.isEmpty()) {
            return "redirect:/dashboard/Employees?error=EmployeeNotFound";
        }
        
        employees profileEmployee = employeeOpt.get();
        
        model.addAttribute("profileEmployee", profileEmployee);
        model.addAttribute("pageTitle", "Employee Profile");
        model.addAttribute("pageContent", "Employee/Profile");
        model.addAttribute("firstName", currentEmployee.getFirst_name());
        model.addAttribute("lastName", currentEmployee.getLast_name());
        model.addAttribute("email", currentEmployee.getEmail());
        model.addAttribute("nickname", currentEmployee.getNickname());
        
        return "Components/layout";
    }
}