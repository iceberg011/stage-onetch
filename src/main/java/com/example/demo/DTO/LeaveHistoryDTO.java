package com.example.demo.DTO;

import java.time.LocalDateTime;

public class LeaveHistoryDTO {
    private Integer leaveId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double leaveDay;
    private String applyReason;
    private String status;
    private LocalDateTime applyTime;
    
    // Getters and Setters
    public Integer getLeaveId() { return leaveId; }
    public void setLeaveId(Integer leaveId) { this.leaveId = leaveId; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public Double getLeaveDay() { return leaveDay; }
    public void setLeaveDay(Double leaveDay) { this.leaveDay = leaveDay; }
    
    public String getApplyReason() { return applyReason; }
    public void setApplyReason(String applyReason) { this.applyReason = applyReason; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getApplyTime() { return applyTime; }
    public void setApplyTime(LocalDateTime applyTime) { this.applyTime = applyTime; }
    
    // Helper methods for PDF
    public String getFormattedStartTime() {
        if (startTime == null) return "N/A";
        return startTime.toString();
    }
    
    public String getFormattedEndTime() {
        if (endTime == null) return "N/A";
        return endTime.toString();
    }
    
    public String getFormattedApplyTime() {
        if (applyTime == null) return "N/A";
        return applyTime.toString();
    }
}