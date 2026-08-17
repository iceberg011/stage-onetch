package com.example.demo.Repository;

import com.example.demo.Entity.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkflowRepository extends JpaRepository<WorkflowInstance, Integer> {

    // Find workflow by leave ID
    @Query("SELECT w FROM WorkflowInstance w WHERE w.leave.workflowinstance_ptr_id = :leaveId")
    Optional<WorkflowInstance> findByLeaveId(@Param("leaveId") Integer leaveId);
    
    // Find workflows by employee
    @Query("SELECT w FROM WorkflowInstance w WHERE w.employee.id = :employeeId")
    List<WorkflowInstance> findByEmployeeId(@Param("employeeId") Long employeeId);
    
    // Find workflows by status
    List<WorkflowInstance> findByApprovalStatus(Short approvalStatus);
    
    // Find pending workflows
    @Query("SELECT w FROM WorkflowInstance w WHERE w.approvalStatus = 0")
    List<WorkflowInstance> findPendingWorkflows();
    
    // Find approved workflows
    @Query("SELECT w FROM WorkflowInstance w WHERE w.approvalStatus = 1")
    List<WorkflowInstance> findApprovedWorkflows();
    
    // Find rejected workflows
    @Query("SELECT w FROM WorkflowInstance w WHERE w.approvalStatus = 2")
    List<WorkflowInstance> findRejectedWorkflows();

    // Sum approved leave days used by an employee in a specific year
    @Query("SELECT COALESCE(SUM(l.leave_day), 0) FROM WorkflowInstance w " +
           "JOIN leave l ON l.workflowinstance_ptr_id = w.leave.workflowinstance_ptr_id " +
           "WHERE w.employee.id = :employeeId " +
           "AND YEAR(l.start_time) = :year " +
           "AND w.approvalStatus = 1")
    Double getUsedLeaveDaysInYearByEmployee(@Param("employeeId") Long employeeId, @Param("year") int year);
}