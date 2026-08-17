package com.example.demo.Service;

import com.example.demo.DTO.LeaveWithEmployeeDTO;
import com.example.demo.Entity.leave;
import com.example.demo.Entity.employees;
import com.example.demo.Entity.WorkflowInstance;
import com.example.demo.Repository.LeaveRepository;
import com.example.demo.Repository.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LeaveService {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private EmployeeService employeeService;

    @Transactional
    public leave createLeaveWithEmployee(leave leaveRequest, Integer employeeId) {
        // Get employee
        Optional<employees> employeeOpt = employeeService.getEmployeeById(Long.valueOf(employeeId));
        if (employeeOpt.isEmpty()) {
            throw new RuntimeException("Employee not found with ID: " + employeeId);
        }
        
        employees employee = employeeOpt.get();
        
        // Save the leave first
        leave savedLeave = leaveRepository.save(leaveRequest);
        
        // Create workflow for this leave (links employee and leave via foreign keys)
        WorkflowInstance workflow = new WorkflowInstance();
        workflow.setEmployee(employee);
        workflow.setLeave(savedLeave);
        workflow.setApprovalStatus((short) 0); // Pending
        workflow.setApprovalTime(java.time.LocalDateTime.now());
        workflow.setApproverInstance("Leave Approval");
        workflow.setApprover("System");
        
        workflowRepository.save(workflow);
        
        return savedLeave;
    }

    public List<LeaveWithEmployeeDTO> searchLeaves(String search) {
        if (search != null && !search.isEmpty()) {
            return leaveRepository.searchLeaveWithEmployee(search);
        }
        return leaveRepository.findAllLeaveWithEmployee();
    }

    public Optional<leave> findById(Integer id) {
        return leaveRepository.findByWorkflowinstance_ptrId(id);
    }

    public void save(leave leave) {
        leaveRepository.save(leave);
    }

    public void deleteById(Integer id) {
        // First delete the workflow
        Optional<WorkflowInstance> workflowOpt = workflowRepository.findByLeaveId(id);
        workflowOpt.ifPresent(workflowRepository::delete);
        
        // Then delete the leave
        leaveRepository.deleteById(id);
    }

    public List<leave> getLeavesByEmployee(Integer employeeId) {
        return leaveRepository.findByEmployeeId(Long.valueOf(employeeId));
    }

    public long countLeavesByEmployee(Integer employeeId) {
        return leaveRepository.countByEmployeeId(Long.valueOf(employeeId));
    }
}