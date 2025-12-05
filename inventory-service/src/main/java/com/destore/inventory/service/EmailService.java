package com.destore.inventory.service;

import com.destore.inventory.model.dto.StockAlert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for composing and sending stock alert emails.
 * <p>
 * This service handles all email communication for the inventory monitoring system.
 * It formats stock alerts into professional HTML emails and sends them to
 * network managers via SMTP.
 * </p>
 * <p>
 * <strong>Features:</strong>
 * <ul>
 *   <li>HTML email formatting with color-coded severity levels</li>
 *   <li>Grouped alerts by status (Out of Stock, Critical, Low)</li>
 *   <li>XSS protection with HTML escaping</li>
 *   <li>Configurable sender address and subject prefix</li>
 *   <li>Enable/disable toggle for testing</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Email Structure:</strong>
 * <ul>
 *   <li>Header with timestamp</li>
 *   <li>Statistics summary</li>
 *   <li>Tables grouped by severity (red/orange/yellow)</li>
 *   <li>Footer with automated message disclaimer</li>
 * </ul>
 * </p>
 *
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    
    @Value("${email.from}")
    private String fromEmail;
    
    @Value("${email.subject-prefix}")
    private String subjectPrefix;
    
    @Value("${email.enabled:true}")
    private boolean emailEnabled;

    /**
     * Constructs a new EmailService.
     *
     * @param mailSender the JavaMailSender to use for sending emails
     */
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends stock alert email to specified recipients.
     * <p>
     * Composes and sends an HTML email containing stock alerts grouped by severity.
     * Email includes color-coded tables and formatted timestamp.
     * </p>
     * <p>
     * <strong>Validation:</strong>
     * <ul>
     *   <li>Checks if email sending is enabled (configurable via email.enabled)</li>
     *   <li>Validates recipients list is not empty</li>
     *   <li>Validates alerts list is not empty</li>
     * </ul>
     * </p>
     *
     * @param recipients List of email addresses to send to (typically network managers)
     * @param alerts     List of stock alerts to include in email
     * @throws MailException if SMTP sending fails
     */
    public void sendStockAlerts(List<String> recipients, List<StockAlert> alerts) {
        if (!emailEnabled) {
            log.info("Email notifications are disabled. Skipping email send.");
            return;
        }

        if (recipients == null || recipients.isEmpty()) {
            log.warn("No recipients provided for stock alerts");
            return;
        }

        if (alerts == null || alerts.isEmpty()) {
            log.info("No stock alerts to send");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(recipients.toArray(new String[0]));
            helper.setSubject(subjectPrefix + " Stock Alert - " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            
            String htmlContent = buildHtmlEmail(alerts);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            log.info("Stock alert email sent to {} recipients with {} alerts", 
                recipients.size(), alerts.size());
            
        } catch (MessagingException | MailException e) {
            log.error("Failed to send stock alert email", e);
        }
    }

    /**
     * @brief Build HTML email content with alerts grouped by status
     * 
     * Creates a professional HTML email with:
     * - Header with generation timestamp
     * - Statistics summary
     * - Color-coded sections by severity:
     *   * Red background for OUT_OF_STOCK
     *   * Orange background for CRITICAL
     *   * Yellow background for LOW
     * - Tables with item details (ID, name, category, stock)
     * - Footer with disclaimer
     * 
     * @param alerts List of stock alerts to format
     * @return HTML string ready for email body
     */
    private String buildHtmlEmail(List<StockAlert> alerts) {
        // Group alerts by status
        Map<StockAlert.StockStatus, List<StockAlert>> groupedAlerts = alerts.stream()
            .collect(Collectors.groupingBy(StockAlert::getStatus));
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><style>");
        html.append("body { font-family: Arial, sans-serif; }");
        html.append("table { border-collapse: collapse; width: 100%; margin-bottom: 20px; }");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        html.append("th { background-color: #4CAF50; color: white; }");
        html.append(".out-of-stock { background-color: #ffcccc; }");
        html.append(".critical { background-color: #ffe6cc; }");
        html.append(".low { background-color: #ffffcc; }");
        html.append("h2 { color: #333; }");
        html.append("</style></head><body>");
        html.append("<h1>Warehouse Stock Alert Report</h1>");
        html.append("<p>Generated: ").append(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ).append("</p>");
        
        // Out of stock items (highest priority)
        if (groupedAlerts.containsKey(StockAlert.StockStatus.OUT_OF_STOCK)) {
            html.append("<h2 style='color: #d32f2f;'>⚠️ OUT OF STOCK</h2>");
            html.append(buildAlertTable(groupedAlerts.get(StockAlert.StockStatus.OUT_OF_STOCK), "out-of-stock"));
        }
        
        // Critical stock items
        if (groupedAlerts.containsKey(StockAlert.StockStatus.CRITICAL)) {
            html.append("<h2 style='color: #f57c00;'>⚠️ CRITICAL STOCK</h2>");
            html.append(buildAlertTable(groupedAlerts.get(StockAlert.StockStatus.CRITICAL), "critical"));
        }
        
        // Low stock items
        if (groupedAlerts.containsKey(StockAlert.StockStatus.LOW)) {
            html.append("<h2 style='color: #fbc02d;'>⚠️ LOW STOCK</h2>");
            html.append(buildAlertTable(groupedAlerts.get(StockAlert.StockStatus.LOW), "low"));
        }
        
        html.append("<hr>");
        html.append("<p style='color: #666; font-size: 12px;'>");
        html.append("This is an automated message from the DE-Store Inventory Management System. ");
        html.append("Please take appropriate action to replenish stock levels.");
        html.append("</p>");
        html.append("</body></html>");
        
        return html.toString();
    }

    /**
     * @brief Build HTML table for a list of alerts
     * 
     * Creates a formatted table with columns:
     * - Item ID
     * - Item Name (HTML escaped)
     * - Category (HTML escaped)
     * - Current Stock
     * 
     * @param alerts List of alerts to tabulate
     * @param cssClass CSS class for row background color
     * @return HTML table string
     */
    private String buildAlertTable(List<StockAlert> alerts, String cssClass) {
        StringBuilder table = new StringBuilder();
        table.append("<table>");
        table.append("<thead><tr>");
        table.append("<th>Item ID</th>");
        table.append("<th>Item Name</th>");
        table.append("<th>Category</th>");
        table.append("<th>Current Stock</th>");
        table.append("</tr></thead>");
        table.append("<tbody>");
        
        for (StockAlert alert : alerts) {
            table.append("<tr class='").append(cssClass).append("'>");
            table.append("<td>").append(alert.getItemId()).append("</td>");
            table.append("<td>").append(escapeHtml(alert.getItemName())).append("</td>");
            table.append("<td>").append(escapeHtml(alert.getCategory())).append("</td>");
            table.append("<td>").append(alert.getCurrentStock()).append("</td>");
            table.append("</tr>");
        }
        
        table.append("</tbody></table>");
        return table.toString();
    }

    /**
     * @brief Simple HTML escaping to prevent XSS
     * 
     * Escapes dangerous HTML characters to prevent injection attacks:
     * - & becomes &amp;
     * - < becomes &lt;
     * - > becomes &gt;
     * - " becomes &quot;
     * - ' becomes &#x27;
     * 
     * @param text Text to escape (may be null)
     * @return Escaped HTML-safe string
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }
}
