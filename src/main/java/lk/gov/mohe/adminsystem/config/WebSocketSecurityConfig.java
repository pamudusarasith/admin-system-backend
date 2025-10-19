package lk.gov.mohe.adminsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        // Ensures that any message sent to the application destination ("/app/**")
        // or any subscription requires an authenticated user.
        messages
                .simpDestMatchers("/app/**").authenticated()
                .simpSubscribeDestMatchers("/user/queue/notifications").authenticated()
                .anyMessage().authenticated();
    }

    @Override
    protected boolean sameOriginDisabled() {
        // Disable CSRF for WebSocket connections. JWT authentication will protect it.
        return true;
    }
}