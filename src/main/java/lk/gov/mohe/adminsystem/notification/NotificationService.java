package lk.gov.mohe.adminsystem.notification;

import lk.gov.mohe.adminsystem.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Sends a notification message to a specific user via RabbitMQ.
     * @param userId The ID of the user to notify.
     * @param notification The notification content.
     */
    public void sendNotification(Integer userId, NotificationDto notification) {
        // We wrap the user ID and notification in a simple payload object.
        NotificationPayload payload = new NotificationPayload(userId, notification);

        // Send to the exchange. The listener will pick it up from the bound queue.
        rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE, "notifications.new", payload);
        log.info("Published notification for user ID: {}", userId);
    }

    // A simple private record to bundle the payload for RabbitMQ.
    private record NotificationPayload(Integer userId, NotificationDto notification) implements Serializable {}
}