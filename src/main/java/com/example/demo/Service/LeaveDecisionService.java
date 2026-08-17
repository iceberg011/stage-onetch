package com.example.demo.Service;

import com.example.demo.Entity.WorkflowInstance;
import com.example.demo.Entity.employees;
import com.example.demo.Entity.leave;
import com.example.demo.Repository.WorkflowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LeaveDecisionService {

    private final LeaveDecisionDataset dataset = new LeaveDecisionDataset();

    private static final Logger log = LoggerFactory.getLogger(LeaveDecisionService.class);

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private EmailService emailService;

    public LeaveDecisionSummary reviewPendingLeaves() {
        List<WorkflowInstance> pendingWorkflows = workflowRepository.findPendingWorkflows();
        List<String> decisions = new ArrayList<>();

        for (WorkflowInstance workflow : pendingWorkflows) {
            String decision = evaluateWorkflowDecision(workflow);
            if (decision == null) {
                continue;
            }

            workflow.setApprovalStatus(decision.equals("APPROVED") ? (short) 1 : (short) 2);
            workflow.setApprover("AI Insights");
            workflow.setApprovalRemark("AI decision: " + decision + " based on policy evaluation");
            workflowRepository.save(workflow);

            employees employee = workflow.getEmployee();
            if (employee != null && employee.getEmail() != null && !employee.getEmail().isBlank()) {
                emailService.sendLeaveDecisionEmail(
                        employee.getEmail(),
                        employee.getFirst_name() != null ? employee.getFirst_name() : "Employee",
                        decision,
                        workflow.getLeave() != null ? workflow.getLeave().getApply_reason() : null,
                        "AI reviewed your request using company leave policy rules."
                );
            }

            dataset.recordDecision(
                    workflow.getLeave() != null ? workflow.getLeave().getWorkflowinstance_ptr_id() : null,
                    employee != null ? employee.getEmail() : null,
                    workflow.getLeave() != null ? workflow.getLeave().getApply_reason() : null,
                    decision,
                    "AI policy evaluation"
            );

            decisions.add("Leave " + (workflow.getLeave() != null ? workflow.getLeave().getWorkflowinstance_ptr_id() : "?") + " => " + decision);
        }

        return new LeaveDecisionSummary(decisions.size(), String.join("\n", decisions));
    }

    public String evaluateWorkflowDecision(WorkflowInstance workflow) {
        ReasonAssessment assessment = assessWorkflow(workflow);
        return assessment == null ? null : assessment.getDecision();
    }

    public ReasonAssessment assessWorkflow(WorkflowInstance workflow) {
        if (workflow == null) {
            return null;
        }

        leave leaveRequest = workflow.getLeave();
        if (leaveRequest == null) {
            return null;
        }

        employees employee = workflow.getEmployee();
        double annualQuota = getAnnualLeaveQuota(employee);
        double usedThisYear = 0.0;

        if (employee != null && employee.getId() != null) {
            Double usedThisYearValue = workflowRepository.getUsedLeaveDaysInYearByEmployee(employee.getId(), LocalDate.now().getYear());
            if (usedThisYearValue != null) {
                usedThisYear = usedThisYearValue;
            }
            if (usedThisYear < 0) {
                usedThisYear = 0;
            }
        }

        return assessLeaveReason(leaveRequest.getApply_reason(), leaveRequest.getLeave_day(), usedThisYear, annualQuota);
    }

    public ReasonAssessment assessLeaveReason(String reason) {
        return assessLeaveReason(reason, null, 0.0, getDefaultAnnualLeaveQuota());
    }

    public ReasonAssessment assessLeaveReason(String reason, Double leaveDays) {
        return assessLeaveReason(reason, leaveDays, 0.0, getDefaultAnnualLeaveQuota());
    }

    public ReasonAssessment assessLeaveReason(String reason, Double leaveDays, double usedThisYear, double annualQuota) {
        if (reason == null || reason.isBlank()) {
            return new ReasonAssessment("LOW", "REJECTED", "Reason text is empty.");
        }

        String normalizedReason = reason.toLowerCase();
        int score = 0;
        List<String> matchedHigh = new ArrayList<>();
        List<String> matchedMedium = new ArrayList<>();

        for (String keyword : dataset.getHighReasonKeywords()) {
            if (normalizedReason.contains(keyword)) {
                score += dataset.getReasonWeights().getOrDefault(keyword, 4);
                matchedHigh.add(keyword);
            }
        }

        for (String keyword : dataset.getMediumReasonKeywords()) {
            if (normalizedReason.contains(keyword)) {
                score += dataset.getReasonWeights().getOrDefault(keyword, 2);
                matchedMedium.add(keyword);
            }
        }

        for (String keyword : dataset.getInvalidReasonKeywords()) {
            if (normalizedReason.contains(keyword)) {
                score -= dataset.getReasonWeights().getOrDefault(keyword, -5);
            }
        }

        if (leaveDays != null && leaveDays > 15) {
            score -= 6;
        }

        if (annualQuota > 0) {
            double usedPercentage = (usedThisYear / annualQuota) * 100;
            if (usedPercentage >= 90) {
                score -= 6;
            } else if (usedPercentage >= 75) {
                score -= 3;
            }
        }

        if (score <= 0) {
            return new ReasonAssessment("LOW", "REJECTED", "Reason is weak or invalid. Matches: " + matchedHigh + matchedMedium + ". Used this year: " + usedThisYear + "/" + annualQuota + " days.");
        }

        if (score >= 6) {
            return new ReasonAssessment("HIGH", "APPROVED", "Strong medical or critical reason. Used this year: " + usedThisYear + "/" + annualQuota + " days.");
        }

        return new ReasonAssessment("MEDIUM", "APPROVED", "Reason is acceptable with moderate confidence. Used this year: " + usedThisYear + "/" + annualQuota + " days.");
    }

    private double getAnnualLeaveQuota(employees employee) {
        if (employee == null) {
            return getDefaultAnnualLeaveQuota();
        }

        Integer group = employee.getLeave_groupe();
        if (group == null) {
            return getDefaultAnnualLeaveQuota();
        }

        switch (group) {
            case 1:
                return 20.0;
            case 2:
                return 25.0;
            case 3:
                return 30.0;
            default:
                return getDefaultAnnualLeaveQuota();
        }
    }

    private double getDefaultAnnualLeaveQuota() {
        return 20.0;
    }

    public static class ReasonAssessment {
        private final String level;
        private final String decision;
        private final String note;

        public ReasonAssessment(String level, String decision, String note) {
            this.level = level;
            this.decision = decision;
            this.note = note;
        }

        public String getLevel() {
            return level;
        }

        public String getDecision() {
            return decision;
        }

        public String getNote() {
            return note;
        }
    }

    public static class LeaveDecisionSummary {
        private final int processedCount;
        private final String message;

        public LeaveDecisionSummary(int processedCount, String message) {
            this.processedCount = processedCount;
            this.message = message;
        }

        public int getProcessedCount() {
            return processedCount;
        }

        public String getMessage() {
            return message;
        }
    }
}
