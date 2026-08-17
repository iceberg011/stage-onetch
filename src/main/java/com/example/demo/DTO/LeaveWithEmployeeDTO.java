package com.example.demo.DTO;

import java.time.LocalDateTime;

public class LeaveWithEmployeeDTO {
    private Integer id;
    private String employeeName;
    private String employeeCode;
    private Long employeeId;           // <-- ADD THIS FIELD
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double leaveDay;
    private String applyReason;
    private Short status;
    private String statusText;
    private String statusBadgeClass;

    // Constructor with status (UPDATE THIS)
    public LeaveWithEmployeeDTO(Integer id, String employeeName, String employeeCode, 
                                Long employeeId, LocalDateTime startTime, LocalDateTime endTime, 
                                Double leaveDay, String applyReason, Short status) {
        this.id = id;
        this.employeeName = employeeName;
        this.employeeCode = employeeCode;
        this.employeeId = employeeId;    // <-- ADD THIS
        this.startTime = startTime;
        this.endTime = endTime;
        this.leaveDay = leaveDay;
        this.applyReason = applyReason;
        this.status = status;
        this.statusText = getStatusTextFromCode(status);
        this.statusBadgeClass = getStatusBadgeClassFromCode(status);
    }

    // Getters...
    public Integer getId() { return id; }
    public String getEmployeeName() { return employeeName; }
    public String getEmployeeCode() { return employeeCode; }
    
    public Long getEmployeeId() { return employeeId; }     // <-- ADD THIS GETTER
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }  // <-- ADD THIS SETTER
    
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Double getLeaveDay() { return leaveDay; }
    public String getApplyReason() { return applyReason; }
    public Short getStatus() { return status; }
    public String getStatusText() { return statusText; }
    public String getStatusBadgeClass() { return statusBadgeClass; }

    private String getStatusTextFromCode(Short status) {
        if (status == null) return "Pending";
        switch (status) {
            case 0: return "Pending";
            case 1: return "Approved";
            case 2: return "Rejected";
            case 3: return "Cancelled";
            default: return "Unknown";
        }
    }

    private String getStatusBadgeClassFromCode(Short status) {
        if (status == null) return "status-pending";
        switch (status) {
            case 0: return "status-pending";
            case 1: return "status-approved";
            case 2: return "status-rejected";
            case 3: return "status-cancelled";
            default: return "status-pending";
        }
    }
}