package com.example.demo.Repository;

import com.example.demo.DTO.LeaveWithEmployeeDTO;
import com.example.demo.Entity.leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRepository extends JpaRepository<leave, Integer> {

    // ===== FIND ALL LEAVES WITH EMPLOYEE =====
    @Query("SELECT new com.example.demo.DTO.LeaveWithEmployeeDTO(" +
           "l.workflowinstance_ptr_id, " +
           "CONCAT(w.employee.first_name, ' ', w.employee.last_name), " +
           "w.employee.emp_code, " +
           "w.employee.id, " +                // <-- ADD EMPLOYEE ID
           "l.start_time, " +
           "l.end_time, " +
           "l.leave_day, " +
           "l.apply_reason, " +
           "w.approvalStatus) " +
           "FROM leave l JOIN WorkflowInstance w ON l.workflowinstance_ptr_id = w.leave.workflowinstance_ptr_id")
    List<LeaveWithEmployeeDTO> findAllLeaveWithEmployee();

    // ===== SEARCH LEAVES WITH EMPLOYEE =====
    @Query("SELECT new com.example.demo.DTO.LeaveWithEmployeeDTO(" +
           "l.workflowinstance_ptr_id, " +
           "CONCAT(w.employee.first_name, ' ', w.employee.last_name), " +
           "w.employee.emp_code, " +
           "w.employee.id, " +                // <-- ADD EMPLOYEE ID
           "l.start_time, " +
           "l.end_time, " +
           "l.leave_day, " +
           "l.apply_reason, " +
           "w.approvalStatus) " +
           "FROM leave l JOIN WorkflowInstance w ON l.workflowinstance_ptr_id = w.leave.workflowinstance_ptr_id " +
           "WHERE LOWER(CONCAT(w.employee.first_name, ' ', w.employee.last_name)) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(l.apply_reason) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<LeaveWithEmployeeDTO> searchLeaveWithEmployee(@Param("search") String search);

    // ===== FIND LEAVE BY ID =====
    @Query("SELECT l FROM leave l WHERE l.workflowinstance_ptr_id = :id")
    Optional<leave> findByWorkflowinstance_ptrId(@Param("id") Integer id);

    // ===== FIND LEAVES BY EMPLOYEE ID =====
    @Query("SELECT l FROM leave l JOIN WorkflowInstance w ON l.workflowinstance_ptr_id = w.leave.workflowinstance_ptr_id WHERE w.employee.id = :employeeId")
    List<leave> findByEmployeeId(@Param("employeeId") Long employeeId);

    // ===== COUNT LEAVES BY EMPLOYEE =====
    @Query("SELECT COUNT(l) FROM leave l JOIN WorkflowInstance w ON l.workflowinstance_ptr_id = w.leave.workflowinstance_ptr_id WHERE w.employee.id = :employeeId")
    long countByEmployeeId(@Param("employeeId") Long employeeId);

    // ===== SEARCH LEAVES BY REASON =====
    @Query("SELECT l FROM leave l WHERE LOWER(l.apply_reason) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<leave> searchByReason(@Param("search") String search);
}