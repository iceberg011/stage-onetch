package com.example.demo.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendLeaveDecisionEmail(String toEmail, String employeeName, String decision, String leaveReason, String note) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Skipped leave decision email because employee email is empty.");
            return;
        }

        if (mailSender == null) {
            log.warn("JavaMailSender is not configured. Leave decision email for {} was not sent.", toEmail);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String normalizedDecision = decision == null ? "UPDATED" : decision.trim().toUpperCase();
            String safeEmployeeName = employeeName != null && !employeeName.isBlank() ? employeeName : "Employee";
            String safeReason = leaveReason != null && !leaveReason.isBlank() ? leaveReason : "Not provided";
            String safeNote = note != null && !note.isBlank() ? note : "No additional note";

            helper.setTo(toEmail);
            helper.setSubject("Leave Request " + normalizedDecision);
            helper.setText(buildEmailHtml(safeEmployeeName, normalizedDecision, safeReason, safeNote), true);

            mailSender.send(message);
            log.info("Leave decision email sent to {} with status {}", toEmail, normalizedDecision);
        } catch (MailException | MessagingException e) {
            log.error("Failed to send leave decision email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    private String buildEmailHtml(String employeeName, String decision, String leaveReason, String note) {
        String statusText = decision.equals("APPROVED") ? "Approved" : "Rejected";
        String statusColor = decision.equals("APPROVED") ? "#16a34a" : "#dc2626";
        String statusBg = decision.equals("APPROVED") ? "#ecfdf5" : "#fef2f2";
        String statusBorder = decision.equals("APPROVED") ? "#86efac" : "#fca5a5";
        String summary = decision.equals("APPROVED")
                ? "Your leave request has been approved. You can proceed with your scheduled time off."
                : "Your leave request has been rejected. Please review the reason and submit a new request if needed.";

        return "<html><body style='margin:0; padding:0; background-color:#f4f7fb; font-family:Arial, sans-serif;'>"
                + "<div style='max-width:680px; margin:32px auto; background:#ffffff; border-radius:16px; overflow:hidden; border:1px solid #e5e7eb;'>"
                + "<div style='background:linear-gradient(135deg, #1f3a8a, #2563eb); padding:28px 30px; color:#ffffff;'>"
                + "<div style='font-size:12px; letter-spacing:1.8px; text-transform:uppercase; opacity:0.8;'>OneTech HR</div>"
                + "<h2 style='margin:12px 0 0; font-size:28px; font-weight:700;'>Leave Request Update</h2>"
                + "</div>"
                + "<div style='padding:30px;'>"
                + "<p style='margin:0 0 18px; font-size:16px; color:#374151;'>Hello <strong>" + employeeName + "</strong>,</p>"
                + "<div style='background:" + statusBg + "; border:1px solid " + statusBorder + "; border-radius:12px; padding:18px 20px; margin:0 0 22px;'>"
                + "<div style='font-size:12px; letter-spacing:1.2px; text-transform:uppercase; color:#374151; font-weight:700; margin-bottom:8px;'>Status</div>"
                + "<div style='display:inline-block; padding:8px 14px; border-radius:999px; background:" + statusColor + "; color:#ffffff; font-weight:700; font-size:14px;'>" + statusText + "</div>"
                + "</div>"
                + "<p style='margin:0 0 18px; font-size:16px; line-height:1.6; color:#1f2937;'>" + summary + "</p>"
                + "<table style='width:100%; border-collapse:collapse; margin:20px 0; background:#f9fafb; border:1px solid #e5e7eb; border-radius:12px; overflow:hidden;'>"
                + "<tr><td style='padding:12px 16px; width:160px; color:#6b7280; font-weight:700; border-bottom:1px solid #e5e7eb;'>Submitted reason</td><td style='padding:12px 16px; color:#111827; border-bottom:1px solid #e5e7eb;'>" + leaveReason + "</td></tr>"
                + "<tr><td style='padding:12px 16px; color:#6b7280; font-weight:700; border-bottom:1px solid #e5e7eb;'>Manager note</td><td style='padding:12px 16px; color:#111827; border-bottom:1px solid #e5e7eb;'>" + note + "</td></tr>"
                + "<tr><td style='padding:12px 16px; color:#6b7280; font-weight:700;'>Decision</td><td style='padding:12px 16px; color:#111827; font-weight:700;'>" + statusText + "</td></tr>"
                + "</table>"
                + "<div style='margin-top:24px; padding-top:18px; border-top:1px solid #e5e7eb; color:#6b7280; font-size:14px;'>"
                + "Thank you,<br><strong>HR / Leave Team</strong>"
                + "</div>"
                + "</div>"
                + "</div>"
                + "</body></html>";
    }
}
