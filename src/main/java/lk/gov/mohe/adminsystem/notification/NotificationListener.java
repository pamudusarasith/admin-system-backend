package lk.gov.mohe.adminsystem.notification;

import lk.gov.mohe.adminsystem.config.RabbitMQConfig;
import lk.gov.mohe.adminsystem.user.User;
import lk.gov.mohe.adminsystem.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    // A record that matches the payload sent by NotificationService
    private record NotificationPayload(Integer userId, NotificationDto notification) implements Serializable {}

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotification(NotificationPayload payload) {
        log.info("Received notification from RabbitMQ for user ID: {}", payload.userId());

        // Find the user to get their username, as STOMP works with the Principal's name.
        userRepository.findById(payload.userId()).ifPresent(user -> {
            // Spring's SimpMessagingTemplate knows how to route to a specific user.
            // It sends the message to the '/user/{username}/queue/notifications' destination.
            String username = user.getUsername();
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/notifications",
                    payload.notification()
            );
            log.info("Sent notification via WebSocket to user: {}", username);
        });
    }
}