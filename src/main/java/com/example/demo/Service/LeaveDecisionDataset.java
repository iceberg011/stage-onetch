package com.example.demo.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaveDecisionDataset {

    private final List<String> validReasonKeywords = new ArrayList<>();
    private final List<String> invalidReasonKeywords = new ArrayList<>();
    private final List<String> mediumReasonKeywords = new ArrayList<>();
    private final List<String> highReasonKeywords = new ArrayList<>();
    private final List<LeaveDecisionRecord> decisionHistory = new ArrayList<>();
    private final Map<String, Integer> reasonWeights = new HashMap<>();

    public LeaveDecisionDataset() {
        Collections.addAll(highReasonKeywords,
                "medical",
                "doctor",
                "hospital",
                "emergency",
                "accident",
                "sick",
                "injury",
                "consultation",
                "surgeries",
                "family emergency",
                "serious health",
                "critical illness",
                "delivery",
                "childbirth",
                "funeral"
        );

        Collections.addAll(mediumReasonKeywords,
                "annual",
                "vacation",
                "personal",
                "family",
                "study",
                "training",
                "exam",
                "appointment",
                "visit",
                "care",
                "school",
                "moving",
                "family commitment"
        );

        Collections.addAll(validReasonKeywords,
                "medical",
                "doctor",
                "hospital",
                "sick",
                "emergency",
                "family emergency",
                "annual",
                "vacation",
                "personal",
                "family",
                "study",
                "training",
                "accident",
                "exam",
                "appointment",
                "care",
                "school",
                "moving",
                "family commitment",
                "critical illness"
        );

        Collections.addAll(invalidReasonKeywords,
                "party",
                "birthday party",
                "wedding ceremony",
                "personal travel",
                "vacation trip",
                "shopping",
                "tour",
                "holiday trip",
                "weekend getaway",
                "concert",
                "festival",
                "pleasure",
                "non urgent leisure"
        );

        for (String keyword : highReasonKeywords) {
            reasonWeights.put(keyword, 4);
        }
        for (String keyword : mediumReasonKeywords) {
            reasonWeights.put(keyword, 2);
        }
        for (String keyword : validReasonKeywords) {
            reasonWeights.putIfAbsent(keyword, 1);
        }
        for (String keyword : invalidReasonKeywords) {
            reasonWeights.put(keyword, -5);
        }
    }

    public List<String> getValidReasonKeywords() {
        return validReasonKeywords;
    }

    public List<String> getInvalidReasonKeywords() {
        return invalidReasonKeywords;
    }

    public List<String> getMediumReasonKeywords() {
        return mediumReasonKeywords;
    }

    public List<String> getHighReasonKeywords() {
        return highReasonKeywords;
    }

    public Map<String, Integer> getReasonWeights() {
        return new HashMap<>(reasonWeights);
    }

    public void recordDecision(Integer leaveId, String employeeEmail, String reason, String decision, String note) {
        if (leaveId == null) {
            return;
        }

        decisionHistory.add(new LeaveDecisionRecord(leaveId, employeeEmail, reason, decision, note));
    }

    public List<LeaveDecisionRecord> getDecisionHistory() {
        return new ArrayList<>(decisionHistory);
    }

    public static class LeaveDecisionRecord {
        private final Integer leaveId;
        private final String employeeEmail;
        private final String reason;
        private final String decision;
        private final String note;

        public LeaveDecisionRecord(Integer leaveId, String employeeEmail, String reason, String decision, String note) {
            this.leaveId = leaveId;
            this.employeeEmail = employeeEmail;
            this.reason = reason;
            this.decision = decision;
            this.note = note;
        }

        public Integer getLeaveId() {
            return leaveId;
        }

        public String getEmployeeEmail() {
            return employeeEmail;
        }

        public String getReason() {
            return reason;
        }

        public String getDecision() {
            return decision;
        }

        public String getNote() {
            return note;
        }
    }
}
