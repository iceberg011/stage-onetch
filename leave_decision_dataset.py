valid_reason_keywords = [
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
    "accident"
]

invalid_reason_keywords = [
    "party",
    "birthday party",
    "wedding ceremony",
    "personal travel",
    "vacation trip",
    "funeral",
    "shopping",
    "tour",
    "holiday trip"
]


def normalize_reason(reason):
    if reason is None:
        return ""
    return str(reason).strip().lower()


def evaluate_leave_decision(reason, leave_days):
    reason_text = normalize_reason(reason)

    if not reason_text:
        return {
            "decision": "REJECTED",
            "reason": "No reason provided",
            "confidence": "low"
        }

    if any(keyword in reason_text for keyword in invalid_reason_keywords):
        if "family funeral" not in reason_text:
            return {
                "decision": "REJECTED",
                "reason": "Reason does not match company leave policy",
                "confidence": "high"
            }

    valid_match = any(keyword in reason_text for keyword in valid_reason_keywords)
    if not valid_match:
        return {
            "decision": "REJECTED",
            "reason": "Reason is not recognized as valid leave justification",
            "confidence": "high"
        }

    if leave_days is not None and float(leave_days) > 15:
        return {
            "decision": "REJECTED",
            "reason": "Leave duration exceeds allowed limit",
            "confidence": "high"
        }

    return {
        "decision": "APPROVED",
        "reason": "Reason and duration match the leave policy",
        "confidence": "medium"
    }


# Example usage
if __name__ == "__main__":
    samples = [
        ("Medical appointment with doctor note", 2),
        ("Family emergency due to hospital consultation", 3),
        ("Birthday party trip", 1),
        ("Annual vacation leave", 7),
        ("Study leave for training course", 5),
    ]

    for reason, days in samples:
        result = evaluate_leave_decision(reason, days)
        print(reason, "->", result)
