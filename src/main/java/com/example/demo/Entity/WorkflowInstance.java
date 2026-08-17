package com.example.demo.Entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "workflow_workflowinstance")
public class WorkflowInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "approval_time")
    private LocalDateTime approvalTime;

    @Column(name = "approval_status")
    private Short approvalStatus;

    @Column(name = "approval_remark", columnDefinition = "TEXT")
    private String approvalRemark;

    @Column(name = "approver", length = 30)
    private String approver;

    @Column(name = "approver_instance", columnDefinition = "TEXT")
    private String approverInstance;

    // Foreign Key to Employee
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    private employees employee;

    // Foreign Key to Leave (workflow_engine_id is actually the leave ID)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_engine_id", referencedColumnName = "workflowinstance_ptr_id")
    private leave leave;

    // Default constructor
    public WorkflowInstance() {}

    // Constructor with required fields
    public WorkflowInstance(employees employee, leave leave) {
        this.employee = employee;
        this.leave = leave;
        this.approvalStatus = 0;
        this.approvalTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getApprovalTime() {
        return approvalTime;
    }

    public void setApprovalTime(LocalDateTime approvalTime) {
        this.approvalTime = approvalTime;
    }

    public Short getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(Short approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getApprovalRemark() {
        return approvalRemark;
    }

    public void setApprovalRemark(String approvalRemark) {
        this.approvalRemark = approvalRemark;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    public String getApproverInstance() {
        return approverInstance;
    }

    public void setApproverInstance(String approverInstance) {
        this.approverInstance = approverInstance;
    }

    public employees getEmployee() {
        return employee;
    }

    public void setEmployee(employees employee) {
        this.employee = employee;
    }

    public Integer getEmployeeId() {
        return employee != null ? Math.toIntExact(employee.getId()) : null;
    }

    public leave getLeave() {
        return leave;
    }

    public void setLeave(leave leave) {
        this.leave = leave;
    }

    public Integer getLeaveId() {
        return leave != null ? leave.getWorkflowinstance_ptr_id() : null;
    }

    // ===== HELPER METHODS =====
    
    // Status constants
    public static final Short STATUS_PENDING = 0;
    public static final Short STATUS_APPROVED = 1;
    public static final Short STATUS_REJECTED = 2;
    public static final Short STATUS_CANCELLED = 3;

    public boolean isPending() {
        return approvalStatus == null || approvalStatus == STATUS_PENDING;
    }

    public boolean isApproved() {
        return approvalStatus != null && approvalStatus == STATUS_APPROVED;
    }

    public boolean isRejected() {
        return approvalStatus != null && approvalStatus == STATUS_REJECTED;
    }

    public boolean isCancelled() {
        return approvalStatus != null && approvalStatus == STATUS_CANCELLED;
    }

    public String getStatusText() {
        if (approvalStatus == null) return "Pending";
        switch (approvalStatus) {
            case 0: return "Pending";
            case 1: return "Approved";
            case 2: return "Rejected";
            case 3: return "Cancelled";
            default: return "Unknown";
        }
    }

    public String getStatusBadgeClass() {
        if (approvalStatus == null) return "status-pending";
        switch (approvalStatus) {
            case 0: return "status-pending";
            case 1: return "status-approved";
            case 2: return "status-rejected";
            case 3: return "status-cancelled";
            default: return "status-pending";
        }
    }

    @Override
    public String toString() {
        return "WorkflowInstance{" +
                "id=" + id +
                ", approvalStatus=" + approvalStatus +
                ", employee=" + (employee != null ? employee.getId() : null) +
                ", leave=" + (leave != null ? leave.getWorkflowinstance_ptr_id() : null) +
                '}';
    }
}