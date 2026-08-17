package com.example.demo.DTO;

import java.time.LocalDateTime;
import java.util.List;

public class EmployeeDetailsDTO {
    private Long id;
    private String fullName;
    private String email;
    private String empCode;
    private String mobile;
    private String departmentName;
    private LocalDateTime lastLogin;
    private List<LeaveHistoryDTO> leaveHistory;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getEmpCode() { return empCode; }
    public void setEmpCode(String empCode) { this.empCode = empCode; }
    
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public List<LeaveHistoryDTO> getLeaveHistory() { return leaveHistory; }
    public void setLeaveHistory(List<LeaveHistoryDTO> leaveHistory) { this.leaveHistory = leaveHistory; }
    
    // Helper methods
    public String getFormattedLastLogin() {
        if (lastLogin == null) return "Never";
        return lastLogin.toString();
    }
    
    public double getTotalLeaveDays() {
        if (leaveHistory == null) return 0;
        return leaveHistory.stream()
                .mapToDouble(LeaveHistoryDTO::getLeaveDay)
                .sum();
    }
    
    public long getLeaveCountByStatus(String status) {
        if (leaveHistory == null) return 0;
        return leaveHistory.stream()
                .filter(l -> status.equals(l.getStatus()))
                .count();
    }
}