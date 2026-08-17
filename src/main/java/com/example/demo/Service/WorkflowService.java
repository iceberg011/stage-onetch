package com.example.demo.Service;

import com.example.demo.Entity.WorkflowInstance;
import com.example.demo.Entity.leave;
import com.example.demo.Entity.employees;
import com.example.demo.Repository.WorkflowRepository;
import com.example.demo.Repository.LeaveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Service
public class WorkflowService {

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Transactional
    public WorkflowInstance approveLeave(Integer leaveId, String approver, String remark) {
        Optional<leave> leaveOpt = leaveRepository.findByWorkflowinstance_ptrId(leaveId);
        if (leaveOpt.isEmpty()) {
            throw new RuntimeException("Leave not found with ID: " + leaveId);
        }
        
        leave leave = leaveOpt.get();
        
        // Find existing workflow
        Optional<WorkflowInstance> workflowOpt = workflowRepository.findByLeaveId(leaveId);
        if (workflowOpt.isEmpty()) {
            throw new RuntimeException("Workflow not found for leave ID: " + leaveId);
        }
        
        WorkflowInstance workflow = workflowOpt.get();
        
        workflow.setApprovalStatus((short) 1); // Approved
        workflow.setApprovalTime(LocalDateTime.now());
        workflow.setApprover(approver);
        workflow.setApprovalRemark(remark);
        
        return workflowRepository.save(workflow);
    }

    public List<WorkflowInstance> getWorkflowsByEmployeeId(Long employeeId) {
        return workflowRepository.findByEmployeeId(employeeId);
    }
    @Transactional
    public WorkflowInstance rejectLeave(Integer leaveId, String approver, String remark) {
        Optional<leave> leaveOpt = leaveRepository.findByWorkflowinstance_ptrId(leaveId);
        if (leaveOpt.isEmpty()) {
            throw new RuntimeException("Leave not found with ID: " + leaveId);
        }
        
        leave leave = leaveOpt.get();
        
        // Find existing workflow
        Optional<WorkflowInstance> workflowOpt = workflowRepository.findByLeaveId(leaveId);
        if (workflowOpt.isEmpty()) {
            throw new RuntimeException("Workflow not found for leave ID: " + leaveId);
        }
        
        WorkflowInstance workflow = workflowOpt.get();
        
        workflow.setApprovalStatus((short) 2); // Rejected
        workflow.setApprovalTime(LocalDateTime.now());
        workflow.setApprover(approver);
        workflow.setApprovalRemark(remark);
        
        return workflowRepository.save(workflow);
    }

    @Transactional
    public WorkflowInstance createWorkflowForLeave(leave leave, employees employee) {
        WorkflowInstance workflow = new WorkflowInstance();
        workflow.setEmployee(employee);
        workflow.setLeave(leave);
        workflow.setApprovalStatus((short) 0); // Pending
        workflow.setApprovalTime(LocalDateTime.now());
        workflow.setApproverInstance("Leave Approval");
        workflow.setApprover("System");
        
        return workflowRepository.save(workflow);
    }

    public Optional<WorkflowInstance> findByLeaveId(Integer leaveId) {
        return workflowRepository.findByLeaveId(leaveId);
    }
}