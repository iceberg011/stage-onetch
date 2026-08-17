package com.example.demo.Controller;

import com.example.demo.Entity.payload;
import com.example.demo.Entity.employees;
import com.example.demo.Service.PayloadService;
import com.example.demo.Service.EmployeeService;
import com.example.demo.Service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Optional;

@Controller
public class PayloadController {

    @Autowired
    private PayloadService payloadService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private WorkflowService workflowService;

    // ===== SHOW ALL PUNCHES =====
    @GetMapping("/dashboard/punches")
    public String showAllPunches(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "punchType", required = false) String punchType,
            HttpSession session,
            Model model) {

        System.out.println("=== GET /dashboard/punches called ===");

        employees currentEmployee = (employees) session.getAttribute("employee");

        if (currentEmployee == null) {
            return "redirect:/signin";
        }

        try {
            List<Object[]> results;

            // Apply filters
            if (search != null && !search.isEmpty()) {
                results = payloadService.searchWithEmployee(search);
            } else if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                results = payloadService.findAllWithEmployeeByDateRange(start, end);
            } else {
                results = payloadService.findAllWithEmployee();
            }

            // Convert to DTOs
            List<PayloadService.PunchWithEmployeeDTO> punches = results.stream()
                    .map(result -> payloadService.getPunchWithEmployee(result))
                    .collect(Collectors.toList());

            // Filter by punch type
            if (punchType != null && !punchType.isEmpty() && !punchType.equals("all")) {
                punches = punches.stream()
                        .filter(p -> p.getPunchType().equalsIgnoreCase(punchType))
                        .collect(Collectors.toList());
            }

            // Calculate stats
            long totalPunches = punches.size();
            long inPunches = punches.stream().filter(p -> p.getPunchType().equals("IN")).count();
            long outPunches = punches.stream().filter(p -> p.getPunchType().equals("OUT")).count();
            long todayPunches = punches.stream()
                    .filter(p -> p.getAttdate() != null && p.getAttdate().equals(LocalDate.now()))
                    .count();

            model.addAttribute("pageTitle", "Punches / Attendance");
            model.addAttribute("pageContent", "Punch/Punches");
            model.addAttribute("employee", currentEmployee);
            model.addAttribute("displayName", currentEmployee.getFirst_name() + " " + currentEmployee.getLast_name());
            model.addAttribute("firstName", currentEmployee.getFirst_name());
            model.addAttribute("lastName", currentEmployee.getLast_name());
            model.addAttribute("email", currentEmployee.getEmail());
            model.addAttribute("nickname", currentEmployee.getNickname());
            model.addAttribute("profileInitial", currentEmployee.getFirst_name() != null ?
                    currentEmployee.getFirst_name().substring(0, 1).toUpperCase() : "U");
            model.addAttribute("employeeId", currentEmployee.getId());

            boolean isAdmin = currentEmployee.getApp_role() != null && currentEmployee.getApp_role() == 1;
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("canAccessDashboard", true);

            model.addAttribute("punches", punches);
            model.addAttribute("totalPunches", totalPunches);
            model.addAttribute("inPunches", inPunches);
            model.addAttribute("outPunches", outPunches);
            model.addAttribute("todayPunches", todayPunches);
            model.addAttribute("searchQuery", search);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("selectedPunchType", punchType);

            return "Components/layout";

        } catch (Exception e) {
            System.err.println("Error loading punches: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading punch data: " + e.getMessage());
            return "Components/layout";
        }
    }

    // ===== VIEW PUNCH DETAILS =====
    @GetMapping("/dashboard/punches/view/{id}")
    public String viewPunch(@PathVariable UUID id, HttpSession session, Model model) {
        System.out.println("=== GET /dashboard/punches/view/" + id + " called ===");

        employees currentEmployee = (employees) session.getAttribute("employee");

        if (currentEmployee == null) {
            return "redirect:/signin";
        }

        try {
            // FIXED: findById expects UUID
            Optional<payload> punchOpt = payloadService.findById(id);

            if (punchOpt.isEmpty()) {
                return "redirect:/dashboard/punches?error=PunchNotFound";
            }

            payload punch = punchOpt.get();

            // FIXED: getEmployeeById doesn't exist, use findById
            // FIXED: convert empid (Integer) to Long for findById
            Long employeeId = Long.valueOf(punch.getEmpid());
            Optional<employees> employeeOpt = employeeService.getEmployeeById(employeeId);

            model.addAttribute("pageTitle", "Punch Details");
            model.addAttribute("pageContent", "Punch/ViewPunch");
            model.addAttribute("employee", currentEmployee);
            model.addAttribute("displayName", currentEmployee.getFirst_name() + " " + currentEmployee.getLast_name());
            model.addAttribute("firstName", currentEmployee.getFirst_name());
            model.addAttribute("lastName", currentEmployee.getLast_name());
            model.addAttribute("email", currentEmployee.getEmail());
            model.addAttribute("nickname", currentEmployee.getNickname());
            model.addAttribute("profileInitial", currentEmployee.getFirst_name() != null ?
                    currentEmployee.getFirst_name().substring(0, 1).toUpperCase() : "U");

            boolean isAdmin = currentEmployee.getApp_role() != null && currentEmployee.getApp_role() == 1;
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("canAccessDashboard", true);

            model.addAttribute("punch", punch);
            model.addAttribute("employeeName", employeeOpt.map(e -> e.getFirst_name() + " " + e.getLast_name()).orElse("N/A"));

            return "Components/layout";

        } catch (Exception e) {
            System.err.println("Error viewing punch: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/dashboard/punches?error=ViewFailed";
        }
    }

    // ===== GET LATEST PUNCH COUNT =====
    @GetMapping("/dashboard/punches/latest-count")
    @ResponseBody
    public java.util.Map<String, Object> getLatestPunchCount() {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            long count = payloadService.count();
            response.put("count", count);
        } catch (Exception e) {
            response.put("count", 0);
        }
        return response;
    }

    // ===== GET LATEST PUNCH =====
    @GetMapping("/dashboard/punches/latest")
    @ResponseBody
    public java.util.Map<String, Object> getLatestPunch() {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            List<payload> latestPunches = payloadService.findAll();
            if (!latestPunches.isEmpty()) {
                // Sort by punchdatetime to get the latest
                latestPunches.sort((a, b) -> {
                    if (a.getPunchdatetime() == null && b.getPunchdatetime() == null) return 0;
                    if (a.getPunchdatetime() == null) return 1;
                    if (b.getPunchdatetime() == null) return -1;
                    return b.getPunchdatetime().compareTo(a.getPunchdatetime());
                });
                payload punch = latestPunches.get(0);

                Long employeeId = Long.valueOf(punch.getEmpid());
                Optional<employees> employeeOpt = employeeService.getEmployeeById(employeeId);
                String employeeName = employeeOpt.map(e -> e.getFirst_name() + " " + e.getLast_name()).orElse("Unknown");

                response.put("id", punch.getId().toString());
                response.put("employeeName", employeeName);
                response.put("punchType", punch.getWorkcode() != null ? punch.getWorkcode() : "IN");
                response.put("punchTime", punch.getPunchtime() != null ? punch.getPunchtime().toString() : "");
                response.put("punchDate", punch.getPunchdate() != null ? punch.getPunchdate().toString() : "");
                response.put("punchDateTime", punch.getPunchdatetime() != null ? punch.getPunchdatetime().toString() : "");
            } else {
                response.put("id", "none");
                response.put("employeeName", "No punches yet");
            }
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        return response;
    }
}