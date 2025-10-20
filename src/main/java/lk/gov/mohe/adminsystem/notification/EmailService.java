package lk.gov.mohe.adminsystem.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender javaMailSender;
  private final SpringTemplateEngine templateEngine;

  /**
   * Sends an email using an HTML template.
   *
   * @param to The recipient's email address.
   * @param subject The subject of the email.
   * @param templateName The name of the Thymeleaf template file (e.g., "welcome-email").
   * @param templateModel A map of variables to be used in the template.
   */
  @Async // Makes the email sending non-blocking, so the user doesn't have to wait.
  public void sendEmailWithTemplate(
      String to, String subject, String templateName, Map<String, Object> templateModel) {
    log.info("Attempting to send email to {} with subject '{}'", to, subject);

    try {
      // 1. Create the Thymeleaf context with variables
      Context context = new Context();
      context.setVariables(templateModel);

      // 2. Process the template to get the HTML content
      String htmlBody = templateEngine.process(templateName + ".html", context);

      // 3. Create and send the email message
      MimeMessage mimeMessage = javaMailSender.createMimeMessage();
      MimeMessageHelper helper =
          new MimeMessageHelper(
              mimeMessage,
              MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, // Allows for inline images, etc.
              StandardCharsets.UTF_8.name());

      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlBody, true); // true indicates the text is HTML

      javaMailSender.send(mimeMessage);
      log.info("Email sent successfully to {}", to);

    } catch (MessagingException e) {
      log.error("Failed to send email to {}", to, e);
      // In a real application, you might want to re-queue this or notify an admin.
    }
  }
}
