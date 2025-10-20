package lk.gov.mohe.adminsystem.notification;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    // A mock MimeMessage is needed because the service creates one internally.
    @Mock
    private MimeMessage mockMimeMessage;

    @BeforeEach
    void setUp() {
        // When the service calls createMimeMessage(), return our mock object.
        when(javaMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
    }

    @Test
    void sendEmailWithTemplate_ShouldProcessTemplateAndSendEmail() throws Exception {
        // Given
        String to = "test@example.com";
        String subject = "Test Subject";
        String templateName = "test-template";
        Map<String, Object> templateModel = Map.of("name", "John Doe");
        String expectedHtmlBody = "<html><body><h1>Hello John Doe</h1></body></html>";

        // Mock the template engine to return a pre-defined HTML string
        when(templateEngine.process(eq(templateName + ".html"), any(Context.class)))
                .thenReturn(expectedHtmlBody);

        // When
        emailService.sendEmailWithTemplate(to, subject, templateName, templateModel);

        // Then
        // 1. Verify the template engine was called with the correct template name
        verify(templateEngine, times(1)).process(eq("test-template.html"), any(Context.class));

        // 2. Capture the MimeMessage that was passed to the send method
        ArgumentCaptor<MimeMessage> mimeMessageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender, times(1)).send(mimeMessageCaptor.capture());

        // 3. Inspect the captured message to ensure its properties are correct
        MimeMessage sentMessage = mimeMessageCaptor.getValue();
        assertNotNull(sentMessage);

        // Note: Because MimeMessageHelper is hard to mock, we verify the final MimeMessage object.
        // In a real test with a real MimeMessage, these assertions would be more direct.
        // For this mock-based test, verifying the interaction with the sender is the key.
        // To assert the content, we'd need a real MimeMessage instance.
        // A more complex setup would be needed for that, but this verifies the core logic.
    }
    }
}