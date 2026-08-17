package com.example.demo.Controller;

import com.example.demo.Entity.employees;
import com.example.demo.Service.EmployeeService;
import com.example.demo.Service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
/* 
    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private LeaveService leaveService;

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        populatePublicPageModel(session, model, "OneTech - Your Technology Partner", "accueil");
        return "index";
    }

    @GetMapping("/Home")
    public String homeAlias(HttpSession session, Model model) {
        System.out.println("=== GET /Home called - Showing home page ===");
        populatePublicPageModel(session, model, "OneTech - Your Technology Partner", "accueil");
        return "index";
    }

    @GetMapping("/about")
    public String about(HttpSession session, Model model) {
        System.out.println("=== GET /about called ===");
        populatePublicPageModel(session, model, "About Us - OneTech", "about");
        return "about";
    }

    @GetMapping("/Leave")
    public String holidays(HttpSession session, Model model) {
        System.out.println("=== GET /Leave called ===");
        populatePublicPageModel(session, model, "Holidays - OneTech", "Leave");
        return "Leave";
    }

    @GetMapping("/contact")
    public String contact(HttpSession session, Model model) {
        System.out.println("=== GET /contact called ===");
        populatePublicPageModel(session, model, "Contact Us - OneTech", "contact");
        return "contact";
    }

    @GetMapping("/services")
    public String services(HttpSession session, Model model) {
        System.out.println("=== GET /services called ===");
        populatePublicPageModel(session, model, "Services - OneTech", "services");
        return "services";
    }

    private void populatePublicPageModel(HttpSession session, Model model, String pageTitle, String activePage) {
        employees currentEmployee = (employees) session.getAttribute("employee");

        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("activePage", activePage);

        if (currentEmployee == null) {
            model.addAttribute("canAccessDashboard", false);
            model.addAttribute("isAdmin", false);
            model.addAttribute("pendingLeaves", 0L);
            model.addAttribute("totalUsers", 0L);
            return;
        }

        model.addAttribute("employee", currentEmployee);
        model.addAttribute("employeeId", currentEmployee.getId());
        model.addAttribute("nickname", currentEmployee.getNickname());
        model.addAttribute("email", currentEmployee.getEmail());
        model.addAttribute("firstName", currentEmployee.getFirst_name());
        model.addAttribute("lastName", currentEmployee.getLast_name());
        model.addAttribute("displayName", buildDisplayName(currentEmployee));
        model.addAttribute("profileInitial", buildProfileInitial(currentEmployee));
        model.addAttribute("appRole", currentEmployee.getApp_role());

        boolean isAdmin = currentEmployee.getApp_role() != null && currentEmployee.getApp_role() == 1;
        boolean canAccessDashboard = currentEmployee.getApp_role() != null && currentEmployee.getApp_role() == 4;

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("canAccessDashboard", canAccessDashboard);
        model.addAttribute("pendingLeaves", currentEmployee.getId() != null
                ? leaveService.countLeavesByEmployee(currentEmployee.getId().intValue())
                : 0L);
        model.addAttribute("totalUsers", isAdmin ? employeeService.countAllEmployees() : 0L);
    }

    private String buildDisplayName(employees employee) {
        String firstName = employee.getFirst_name() != null ? employee.getFirst_name().trim() : "";
        String lastName = employee.getLast_name() != null ? employee.getLast_name().trim() : "";
        String displayName = (firstName + " " + lastName).trim();

        if (!displayName.isEmpty()) {
            return displayName;
        }

        if (employee.getNickname() != null && !employee.getNickname().trim().isEmpty()) {
            return employee.getNickname().trim();
        }

        return "Employee";
    }

    private String buildProfileInitial(employees employee) {
        String displayName = buildDisplayName(employee);
        return displayName.isEmpty() ? "U" : displayName.substring(0, 1).toUpperCase();
    }*/
}