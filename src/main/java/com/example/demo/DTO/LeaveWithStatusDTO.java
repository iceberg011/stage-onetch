package com.example.demo.DTO;

import com.example.demo.Entity.leave;

public class LeaveWithStatusDTO {
    private leave leave;
    private Short status;
    private String statusText;
    private String statusBadgeClass;

    public LeaveWithStatusDTO(leave leave, Short status) {
        this.leave = leave;
        this.status = status;
        this.statusText = getStatusTextFromCode(status);
        this.statusBadgeClass = getStatusBadgeClassFromCode(status);
    }

    public leave getLeave() {
        return leave;
    }

    public void setLeave(leave leave) {
        this.leave = leave;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public boolean isPending() {
        return status == null || status == 0;
    }

    public boolean canEditOrDelete() {
        return isPending();
    }

    public String getStatusText() {
        return statusText;
    }

    public String getStatusBadgeClass() {
        return statusBadgeClass;
    }

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

    public Integer getId() {
        return leave != null ? leave.getWorkflowinstance_ptr_id() : null;
    }

    public String getApplyReason() {
        return leave != null ? leave.getApply_reason() : null;
    }

    public Double getLeaveDay() {
        return leave != null ? leave.getLeave_day() : 0;
    }

    public java.time.LocalDateTime getStartTime() {
        return leave != null ? leave.getStart_time() : null;
    }

    public java.time.LocalDateTime getEndTime() {
        return leave != null ? leave.getEnd_time() : null;
    }
}