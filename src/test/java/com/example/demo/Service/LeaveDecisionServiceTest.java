package com.example.demo.Service;

import com.example.demo.Entity.WorkflowInstance;
import com.example.demo.Entity.employees;
import com.example.demo.Entity.leave;
import com.example.demo.Repository.WorkflowRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveDecisionServiceTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @InjectMocks
    private LeaveDecisionService leaveDecisionService;

    @Test
    void reviewPendingLeaves_shouldReturnSummaryForProcessedRequests() {
        WorkflowInstance workflow = new WorkflowInstance();
        leave leaveRequest = new leave();
        leaveRequest.setApply_reason("Medical appointment for my daughter with doctor note");
        leaveRequest.setLeave_day(2.0);
        workflow.setLeave(leaveRequest);

        employees employee = new employees();
        employee.setEmail("employee@example.com");
        employee.setIs_active(true);
        employee.setLeave_groupe(1);
        workflow.setEmployee(employee);

        when(workflowRepository.findPendingWorkflows()).thenReturn(List.of(workflow));

        LeaveDecisionService.LeaveDecisionSummary summary = leaveDecisionService.reviewPendingLeaves();

        assertNotNull(summary);
        assertNotNull(summary.getMessage());
    }

    @Test
    void assessReasonLevel_shouldClassifyHighQualityMedicalReason() {
        LeaveDecisionService.ReasonAssessment assessment = leaveDecisionService.assessLeaveReason(
                "Medical emergency due to severe migraine and hospital consultation");

        assertEquals("HIGH", assessment.getLevel());
        assertEquals("APPROVED", assessment.getDecision());
    }

    @Test
    void assessReasonLevel_shouldRejectLowQualityReason() {
        LeaveDecisionService.ReasonAssessment assessment = leaveDecisionService.assessLeaveReason(
                "I want to travel and enjoy vacation trip with friends");

        assertEquals("LOW", assessment.getLevel());
        assertEquals("REJECTED", assessment.getDecision());
    }

    @Test
    void assessReasonLevel_shouldRejectWhenYearlyLeaveUsageIsVeryHigh() {
        employees employee = new employees();
        employee.setId(10L);
        employee.setLeave_groupe(1);

        WorkflowInstance workflow = new WorkflowInstance();
        workflow.setEmployee(employee);

        leave leaveRequest = new leave();
        leaveRequest.setApply_reason("Annual vacation for family travel");
        leaveRequest.setLeave_day(3.0);
        workflow.setLeave(leaveRequest);

        when(workflowRepository.getUsedLeaveDaysInYearByEmployee(10L, java.time.LocalDate.now().getYear()))
                .thenReturn(18.0);

        LeaveDecisionService.ReasonAssessment assessment = leaveDecisionService.assessWorkflow(workflow);

        assertNotNull(assessment);
        assertEquals("LOW", assessment.getLevel());
        assertEquals("REJECTED", assessment.getDecision());
    }
}
