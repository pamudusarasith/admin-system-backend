package lk.gov.mohe.adminsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // These are prefixes for messages that are bound for the message broker (e.g., to a user's queue)
        config.enableSimpleBroker("/queue");
        // This is the prefix for messages from the client to the server (e.g., if a user sends a message)
        config.setApplicationDestinationPrefixes("/app");
        // This allows for sending messages to specific users.
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // This is the endpoint that the client will connect to.
        // `withSockJS()` provides a fallback for browsers that don't support WebSockets.
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}