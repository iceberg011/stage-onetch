package com.example.demo.Controller;

import com.example.demo.DTO.EmployeeDetailsDTO;
import com.example.demo.DTO.LeaveHistoryDTO;
import com.example.demo.DTO.LeaveWithEmployeeDTO;
import com.example.demo.DTO.LeaveWithStatusDTO;
import com.example.demo.Entity.leave;
import com.example.demo.Entity.employees;
import com.example.demo.Entity.WorkflowInstance;
import com.example.demo.Service.EmployeeService;
import com.example.demo.Service.LeaveDecisionService;
import com.example.demo.Service.LeaveService;
import com.example.demo.Service.WorkflowService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.awt.Color;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import java.util.List;

@Controller
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private LeaveDecisionService leaveDecisionService;

    @Autowired
    private EmployeeService employeeService;

    private boolean canReviewLeaves(employees employee) {
        return employee != null && (employee.getApp_role() != null &&
                (employee.getApp_role() == 1 || employee.getApp_role() == 2 || employee.getApp_role() == 3));
    }

    // ===== REDIRECT ALL-LEAVES TO LEAVES =====
    @GetMapping("/dashboard/all-leaves")
    public String redirectAllLeaves() {
        System.out.println("=== GET /dashboard/all-leaves - Redirecting to /dashboard/leaves ===");
        return "redirect:/dashboard/leaves";
    }

    // ===== DASHBOARD LEAVES LIST =====
    @GetMapping("/dashboard/leaves")
    public String listLeaves(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "sort", required = false) String sort,
            HttpSession session,
            Model model) {
        
        System.out.println("=== GET /dashboard/leaves called ===");
        
        employees currentEmployee = (employees) session.getAttribute("employee");
        
        if (currentEmployee == null) {
            return "redirect:/signin";
        }
        
        try {
            java.util.List<LeaveWithEmployeeDTO> allLeaves = leaveService.searchLeaves(search);
            
            if (status != null && !status.equals("all") && !status.isEmpty()) {
                Short statusCode = null;
                switch (status) {
                    case "pending": statusCode = 0; break;
                    case "approved": statusCode = 1; break;
                    case "rejected": statusCode = 2; break;
                    case "cancelled": statusCode = 3; break;
                }
                if (statusCode != null) {
                    final Short finalStatusCode = statusCode;
                    allLeaves = allLeaves.stream()
                            .filter(l -> l.getStatus() != null && l.getStatus().equals(finalStatusCode))
                            .collect(Collectors.toList());
                }
            }
            
            if (sort != null && sort.equals("asc")) {
                allLeaves.sort((a, b) -> {
                    if (a.getStartTime() == null || b.getStartTime() == null) return 0;
                    return a.getStartTime().compareTo(b.getStartTime());
                });
            } else {
                allLeaves.sort((a, b) -> {
                    if (a.getStartTime() == null || b.getStartTime() == null) return 0;
                    return b.getStartTime().compareTo(a.getStartTime());
                });
            }
            
            long pendingCount = allLeaves.stream()
                    .filter(l -> l.getStatus() != null && l.getStatus() == 0)
                    .count();
            long approvedCount = allLeaves.stream()
                    .filter(l -> l.getStatus() != null && l.getStatus() == 1)
                    .count();
            long rejectedCount = allLeaves.stream()
                    .filter(l -> l.getStatus() != null && l.getStatus() == 2)
                    .count();
            double totalDays = allLeaves.stream()
                    .mapToDouble(LeaveWithEmployeeDTO::getLeaveDay)
                    .sum();
            
            model.addAttribute("pageTitle", "Leave Management");
            model.addAttribute("pageContent", "LeaveBack/AllLeaves");
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
            model.addAttribute("canReviewLeaves", canReviewLeaves(currentEmployee));
            model.addAttribute("canAccessDashboard", true);
            model.addAttribute("leaves", allLeaves);
            model.addAttribute("leaveCount", allLeaves.size());
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("approvedCount", approvedCount);
            model.addAttribute("rejectedCount", rejectedCount);
            model.addAttribute("totalLeaveDays", totalDays);
            model.addAttribute("searchQuery", search);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("selectedSort", sort);
            
            return "Components/layout";
            
        } catch (Exception e) {
            System.err.println("Error listing leaves: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading leave data: " + e.getMessage());
            return "Components/layout";
        }
    }

    // ===== VIEW LEAVE (Redirect to list) =====
    @GetMapping("/dashboard/leaves/view/{id}")
    public String viewLeave(@PathVariable Integer id, HttpSession session) {
        System.out.println("=== GET /dashboard/leaves/view/" + id + " called ===");
        
        employees currentEmployee = (employees) session.getAttribute("employee");
        
        if (currentEmployee == null) {
            return "redirect:/signin";
        }
        
        return "redirect:/dashboard/leaves";
    }

    // ===== GET EMPLOYEE DETAILS FOR POPUP =====
    @GetMapping("/api/employee/{id}/details")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getEmployeeDetails(@PathVariable Long id, HttpSession session) {
        System.out.println("=== GET /api/employee/" + id + "/details called ===");
        
        employees currentEmployee = (employees) session.getAttribute("employee");
        
        if (currentEmployee == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }
        
        try {
            EmployeeDetailsDTO details = employeeService.getEmployeeDetails(id);
            
            if (details == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Employee not found"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", details.getId());
            response.put("fullName", details.getFullName());
            response.put("email", details.getEmail());
            response.put("empCode", details.getEmpCode());
            response.put("mobile", details.getMobile());
            response.put("departmentName", details.getDepartmentName());
            response.put("lastLogin", details.getLastLogin());
            response.put("leaveHistory", details.getLeaveHistory());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error fetching employee details: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error fetching employee details: " + e.getMessage()));
        }
    }

    // ===== EXPORT EMPLOYEE PDF =====
    @GetMapping("/api/employee/{id}/export-pdf")
    public void exportEmployeePDF(@PathVariable Long id, HttpServletResponse response, HttpSession session) {
        System.out.println("=== GET /api/employee/" + id + "/export-pdf called ===");
        
        employees currentEmployee = (employees) session.getAttribute("employee");
        
        if (currentEmployee == null) {
            try {
                response.sendRedirect("/signin");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        
        try {
            EmployeeDetailsDTO details = employeeService.getEmployeeDetails(id);
            
            if (details == null) {
                response.setContentType("text/html");
                response.getWriter().write("Employee not found");
                return;
            }
            
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", 
                    "attachment; filename=employee_" + id + "_details.pdf");
            
            generateEmployeePDF(response, details);
            
            System.out.println("PDF exported successfully for employee: " + details.getFullName());
            
        } catch (Exception e) {
            System.err.println("Error exporting PDF: " + e.getMessage());
            e.printStackTrace();
            try {
                response.setContentType("text/html");
                response.getWriter().write("Error generating PDF: " + e.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    // ===== PDF GENERATION =====
    private void generateEmployeePDF(HttpServletResponse response, EmployeeDetailsDTO details) 
            throws DocumentException, IOException {
        
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        
        document.open();
        
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        titleFont.setSize(20);
        titleFont.setColor(new Color(47, 121, 255));
        
        Paragraph title = new Paragraph("Employee Details Report", titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1.5f, 3.5f});
        infoTable.setSpacingAfter(20);
        
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        labelFont.setSize(10);
        labelFont.setColor(Color.GRAY);
        
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA);
        valueFont.setSize(11);
        
        addInfoRow(infoTable, "Employee Name:", details.getFullName(), labelFont, valueFont);
        addInfoRow(infoTable, "Employee Code:", details.getEmpCode() != null ? details.getEmpCode() : "N/A", labelFont, valueFont);
        addInfoRow(infoTable, "Email:", details.getEmail(), labelFont, valueFont);
        addInfoRow(infoTable, "Phone:", details.getMobile() != null ? details.getMobile() : "N/A", labelFont, valueFont);
        addInfoRow(infoTable, "Department:", details.getDepartmentName() != null ? details.getDepartmentName() : "N/A", labelFont, valueFont);
        addInfoRow(infoTable, "Last Login:", details.getFormattedLastLogin(), labelFont, valueFont);
        
        document.add(infoTable);
        
        PdfPTable statsTable = new PdfPTable(4);
        statsTable.setWidthPercentage(100);
        statsTable.setWidths(new float[]{1, 1, 1, 1});
        statsTable.setSpacingAfter(15);
        
        Font statFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        statFont.setSize(11);
        statFont.setColor(Color.WHITE);
        
        addStatsCell(statsTable, "Total Leaves: " + details.getLeaveHistory().size(), new Color(47, 121, 255));
        addStatsCell(statsTable, "Pending: " + details.getLeaveCountByStatus("Pending"), new Color(217, 119, 6));
        addStatsCell(statsTable, "Approved: " + details.getLeaveCountByStatus("Approved"), new Color(22, 163, 74));
        addStatsCell(statsTable, "Total Days: " + String.format("%.1f", details.getTotalLeaveDays()), new Color(138, 43, 226));
        
        document.add(statsTable);
        
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        sectionFont.setSize(14);
        sectionFont.setColor(new Color(26, 26, 46));
        
        Paragraph section = new Paragraph("Leave History", sectionFont);
        section.setSpacingBefore(10);
        section.setSpacingAfter(10);
        document.add(section);
        
        PdfPTable leaveTable = new PdfPTable(6);
        leaveTable.setWidthPercentage(100);
        leaveTable.setWidths(new float[]{1.5f, 1.5f, 1.0f, 2.0f, 1.2f, 1.5f});
        
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        headerFont.setSize(9);
        headerFont.setColor(Color.WHITE);
        
        String[] headers = {"Start Date", "End Date", "Days", "Reason", "Status", "Applied On"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new Color(47, 121, 255));
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            leaveTable.addCell(cell);
        }
        
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA);
        dataFont.setSize(9);
        
        for (LeaveHistoryDTO leave : details.getLeaveHistory()) {
            leaveTable.addCell(new PdfPCell(new Phrase(leave.getFormattedStartTime(), dataFont)));
            leaveTable.addCell(new PdfPCell(new Phrase(leave.getFormattedEndTime(), dataFont)));
            leaveTable.addCell(new PdfPCell(new Phrase(String.valueOf(leave.getLeaveDay()), dataFont)));
            
            String reason = leave.getApplyReason();
            if (reason != null && reason.length() > 50) {
                reason = reason.substring(0, 47) + "...";
            }
            leaveTable.addCell(new PdfPCell(new Phrase(reason != null ? reason : "N/A", dataFont)));
            
            String status = leave.getStatus() != null ? leave.getStatus() : "Pending";
            PdfPCell statusCell = new PdfPCell(new Phrase(status, dataFont));
            statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            statusCell.setPadding(5);
            
            if ("Approved".equals(status)) {
                statusCell.setBackgroundColor(new Color(220, 252, 231));
            } else if ("Rejected".equals(status)) {
                statusCell.setBackgroundColor(new Color(254, 226, 226));
            } else if ("Pending".equals(status)) {
                statusCell.setBackgroundColor(new Color(254, 243, 199));
            } else if ("Cancelled".equals(status)) {
                statusCell.setBackgroundColor(new Color(241, 243, 245));
            }
            leaveTable.addCell(statusCell);
            
            leaveTable.addCell(new PdfPCell(new Phrase(leave.getFormattedApplyTime(), dataFont)));
        }
        
        document.add(leaveTable);
        
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE);
        footerFont.setSize(8);
        footerFont.setColor(Color.GRAY);
        
        Paragraph footer = new Paragraph("Generated on: " + 
            LocalDateTime.now().toString(), footerFont);
        footer.setAlignment(Paragraph.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        document.add(footer);
        
        document.close();
    }

    private void addInfoRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4);
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(valueCell);
    }

    private void addStatsCell(PdfPTable table, String text, Color color) {
        Font statFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        statFont.setSize(11);
        statFont.setColor(Color.WHITE);
        
        PdfPCell cell = new PdfPCell(new Phrase(text, statFont));
        cell.setBackgroundColor(color);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    // ===== APPROVE LEAVE =====
    @GetMapping("/dashboard/leaves/approve/{id}")
    public String approveLeave(@PathVariable Integer id, HttpSession session) {
        System.out.println("=== GET /dashboard/leaves/approve/" + id + " called ===");
        
        employees currentEmployee = (employees) session.getAttribute("employee");
        
        if (currentEmployee == null) {
            return "redirect:/signin";
        }
        
        if (!canReviewLeaves(currentEmployee)) {
            return "redirect:/dashboard/leaves?error=Unauthorized";
        }
        
        try {
            workflowService.approveLeave(
                id, 
                currentEmployee.getFirst_name() + " " + currentEmployee.getLast_name(),
                "Approved by " + currentEmployee.getFirst_name()
            );
            return "redirect:/dashboard/leaves?success=LeaveApproved";
        } catch (Exception e) {
            System.err.println("Error approving leave: " + e.getMessage());
            return "redirect:/dashboard/leaves?error=ApprovalFailed";
        }
    }

    // ===== REJECT LEAVE =====
    @GetMapping("/dashboard/leaves/reject/{id}")
    public String rejectLeave(@PathVariable Integer id, HttpSession session) {
        System.out.println("=== GET /dashboard/leaves/reject/" + id + " called ===");
        
        employees currentEmployee = (employees) session.getAttribute("employee");
        
        if (currentEmployee == null) {
            return "redirect:/signin";
        }
        
        if (!canReviewLeaves(currentEmployee)) {
            return "redirect:/dashboard/leaves?error=Unauthorized";
        }
        
        try {
            workflowService.rejectLeave(
                id,
                currentEmployee.getFirst_name() + " " + currentEmployee.getLast_name(),
                "Rejected by " + currentEmployee.getFirst_name()
            );
            return "redirect:/dashboard/leaves?success=LeaveRejected";
        } catch (Exception e) {
            System.err.println("Error rejecting leave: " + e.getMessage());
            return "redirect:/dashboard/leaves?error=RejectionFailed";
        }
    }

    // ===== DELETE LEAVE =====
    @GetMapping("/dashboard/leaves/delete/{id}")
    public String deleteLeave(@PathVariable Integer id, HttpSession session) {
        System.out.println("=== GET /dashboard/leaves/delete/" + id + " called ===");
        
        employees currentEmployee = (employees) session.getAttribute("employee");
        
        if (currentEmployee == null) {
            return "redirect:/signin";
        }
        
        try {
            Optional<leave> leaveOpt = leaveService.findById(id);
            
            if (leaveOpt.isEmpty()) {
                return "redirect:/dashboard/leaves?error=LeaveNotFound";
            }
            
            Optional<WorkflowInstance> workflowOpt = workflowService.findByLeaveId(id);
            if (workflowOpt.isPresent()) {
                WorkflowInstance workflow = workflowOpt.get();
                if (!workflow.isPending()) {
                    return "redirect:/dashboard/leaves?error=CannotDelete";
                }
            }
            
            leaveService.deleteById(id);
            return "redirect:/dashboard/leaves?success=LeaveDeleted";
            
        } catch (Exception e) {
            System.err.println("Error deleting leave: " + e.getMessage());
            return "redirect:/dashboard/leaves?error=DeleteFailed";
        }
    }

    // ===== EDIT LEAVE =====
    @GetMapping("/dashboard/leaves/edit/{id}")
    public String showEditLeaveForm(@PathVariable Integer id, HttpSession session, Model model) {
        System.out.println("=== GET /dashboard/leaves/edit/" + id + " called ===");
        
        employees currentEmployee = (employees) session.getAttribute("employee");
        
        if (currentEmployee == null) {
            return "redirect:/signin";
        }
        
        try {
            Optional<leave> leaveOpt = leaveService.findById(id);
            
            if (leaveOpt.isEmpty()) {
                return "redirect:/dashboard/leaves?error=LeaveNotFound";
            }
            
            leave leave = leaveOpt.get();
            Optional<WorkflowInstance> workflowOpt = workflowService.findByLeaveId(id);
            
            if (workflowOpt.isPresent()) {
                WorkflowInstance workflow = workflowOpt.get();
                if (!workflow.isPending()) {
                    return "redirect:/dashboard/leaves?error=CannotEdit";
                }
            } else {
                return "redirect:/dashboard/leaves?error=NoWorkflow";
            }
            
            model.addAttribute("pageTitle", "Edit Leave Request");
            model.addAttribute("pageContent", "Leave/EditLeave");
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
            model.addAttribute("leave", leave);
            
            return "Components/layout";
            
        } catch (Exception e) {
            System.err.println("Error loading edit form: " + e.getMessage());
            return "redirect:/dashboard/leaves?error=EditFailed";
        }
    }

    // ===== SHOW CREATE LEAVE FORM =====
    @GetMapping("/Leaves")
    public String showFrontLeaveForm(HttpSession session, Model model) {
        System.out.println("=== GET /Leaves called ===");
        
        employees currentEmployee = (employees) session.getAttribute("employee");
        
        if (currentEmployee == null) {
            return "redirect:/signin";
        }
        
        model.addAttribute("pageTitle", "New Leave Request");
        model.addAttribute("pageContent", "Leave/NewLeave");
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
        model.addAttribute("leave", new leave());
        
        return "Components/layout";
    }

    // ===== CREATE LEAVE =====
    @PostMapping("/Leaves")
    public String createFrontLeave(
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            @RequestParam("reason") String applyReason,
            @RequestParam(value = "leaveDays", required = false) Double leaveDay,
            @RequestParam(value = "leaveType", required = false) String leaveType,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment,
            HttpSession session,
            Model model) {
        
        System.out.println("=== POST /Leaves called ===");
        
        employees currentEmployee = (employees) session.getAttribute("employee");
        
        if (currentEmployee == null) {
            return "redirect:/signin";
        }
        
        if (startTime == null || startTime.isEmpty()) {
            return getCreateLeaveFormWithError(session, model, currentEmployee, "Start date and time is required");
        }
        
        if (endTime == null || endTime.isEmpty()) {
            return getCreateLeaveFormWithError(session, model, currentEmployee, "End date and time is required");
        }
        
        if (applyReason == null || applyReason.trim().isEmpty()) {
            return getCreateLeaveFormWithError(session, model, currentEmployee, "Reason is required");
        }
        
        if (applyReason.trim().length() < 10) {
            return getCreateLeaveFormWithError(session, model, currentEmployee, "Please provide a more detailed reason (at least 10 characters)");
        }
        
        if (leaveDay == null || leaveDay < 0.5) {
            return getCreateLeaveFormWithError(session, model, currentEmployee, "Leave days must be at least 0.5 days");
        }
        
        try {
            leave newLeave = new leave();
            
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime start = LocalDateTime.parse(startTime, formatter);
            LocalDateTime end = LocalDateTime.parse(endTime, formatter);
            
            if (start.isAfter(end)) {
                return getCreateLeaveFormWithError(session, model, currentEmployee, "Start date cannot be after end date");
            }
            
            newLeave.setStart_time(start);
            newLeave.setEnd_time(end);
            newLeave.setApply_reason(applyReason.trim());
            newLeave.setApply_time(LocalDateTime.now());
            newLeave.setLeave_day(leaveDay);
            
            if (attachment != null && !attachment.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + attachment.getOriginalFilename();
                newLeave.setAttachement(fileName);
            }
            
            leaveService.createLeaveWithEmployee(
                newLeave, 
                Math.toIntExact(currentEmployee.getId())
            );
            
            return "redirect:/dashboard/leaves?success=LeaveCreated";
            
        } catch (DateTimeParseException e) {
            System.err.println("Date parsing error: " + e.getMessage());
            return getCreateLeaveFormWithError(session, model, currentEmployee, "Invalid date format. Please use the date picker.");
            
        } catch (Exception e) {
            System.err.println("Error creating leave: " + e.getMessage());
            e.printStackTrace();
            return getCreateLeaveFormWithError(session, model, currentEmployee, "Error creating leave: " + e.getMessage());
        }
    }

    private String getCreateLeaveFormWithError(HttpSession session, Model model, employees currentEmployee, String error) {
        model.addAttribute("pageTitle", "Add Leave Request");
        model.addAttribute("pageContent", "Leave/NewLeave");
        model.addAttribute("employee", currentEmployee);
        model.addAttribute("displayName", currentEmployee.getFirst_name() + " " + currentEmployee.getLast_name());
        model.addAttribute("firstName", currentEmployee.getFirst_name());
        model.addAttribute("lastName", currentEmployee.getLast_name());
        model.addAttribute("email", currentEmployee.getEmail());
        model.addAttribute("nickname", currentEmployee.getNickname());
        model.addAttribute("profileInitial", currentEmployee.getFirst_name() != null ? 
                currentEmployee.getFirst_name().substring(0, 1).toUpperCase() : "U");
        model.addAttribute("error", error);
        
        boolean isAdmin = currentEmployee.getApp_role() != null && currentEmployee.getApp_role() == 1;
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("canAccessDashboard", true);
        model.addAttribute("leave", new leave());
        
        return "Components/layout";
    }




    // ===== 5. AI REVIEW LEAVES =====
    @PostMapping("/dashboard/leaves/ai-review")
    public String aiReviewLeaves(HttpSession session) {
        employees currentEmployee = (employees) session.getAttribute("employee");

        if (currentEmployee == null) {
            return "redirect:/signin";
        }

        if (!canReviewLeaves(currentEmployee)) {
            return "redirect:/dashboard/leaves?error=Unauthorized";
        }

        try {
            LeaveDecisionService.LeaveDecisionSummary summary = leaveDecisionService.reviewPendingLeaves();
            if (summary.getProcessedCount() == 0) {
                return "redirect:/dashboard/leaves?error=NoPendingLeavesToReview";
            }
            return "redirect:/dashboard/leaves?success=AIReviewCompleted";
        } catch (Exception e) {
            System.err.println("Error running AI review: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/dashboard/leaves?error=AIReviewFailed";
        }
    }



    // ===== 2. MY LEAVES (User View - Show only logged-in user's leaves) =====
    @GetMapping("/ShowLeave")
    public String myLeaves(HttpSession session, Model model) {
        System.out.println("=== GET /ShowLeave called ===");
        
        employees currentEmployee = (employees) session.getAttribute("employee");
        
        if (currentEmployee == null) {
            return "redirect:/signin";
        }
        
        try {
            Integer employeeId = Math.toIntExact(currentEmployee.getId());
            
            // Get workflows for this employee (contains status)
            List<WorkflowInstance> workflows = workflowService.getWorkflowsByEmployeeId(Long.valueOf(employeeId));
            
            // Get leaves for this employee
            List<leave> myLeaves = leaveService.getLeavesByEmployee(employeeId);
            
            // Create a combined list with status from workflow
            List<LeaveWithStatusDTO> leavesWithStatus = new java.util.ArrayList<>();
            for (leave l : myLeaves) {
                WorkflowInstance workflow = workflows.stream()
                        .filter(w -> w.getLeave() != null && 
                                    w.getLeave().getWorkflowinstance_ptr_id().equals(l.getWorkflowinstance_ptr_id()))
                        .findFirst()
                        .orElse(null);
                
                Short status = workflow != null ? workflow.getApprovalStatus() : 0;
                leavesWithStatus.add(new LeaveWithStatusDTO(l, status));
            }
            
            // Calculate stats
            long pendingCount = leavesWithStatus.stream()
                    .filter(l -> l.getStatus() != null && l.getStatus() == 0)
                    .count();
            long approvedCount = leavesWithStatus.stream()
                    .filter(l -> l.getStatus() != null && l.getStatus() == 1)
                    .count();
            long rejectedCount = leavesWithStatus.stream()
                    .filter(l -> l.getStatus() != null && l.getStatus() == 2)
                    .count();
            double totalDays = leavesWithStatus.stream()
                    .mapToDouble(l -> l.getLeave() != null && l.getLeave().getLeave_day() != null ? l.getLeave().getLeave_day() : 0)
                    .sum();
            
            model.addAttribute("pageTitle", "My Leaves");
            model.addAttribute("pageContent", "Leave/ShowLeave");
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
            
            model.addAttribute("leaves", leavesWithStatus);
            model.addAttribute("leaveCount", leavesWithStatus.size());
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("approvedCount", approvedCount);
            model.addAttribute("rejectedCount", rejectedCount);
            model.addAttribute("totalLeaveDays", totalDays);
            
            return "Leave/ShowLeave";
            
        } catch (Exception e) {
            System.err.println("Error loading my leaves: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading your leaves: " + e.getMessage());
            return "Leave/ShowLeave";
        }
    }

}