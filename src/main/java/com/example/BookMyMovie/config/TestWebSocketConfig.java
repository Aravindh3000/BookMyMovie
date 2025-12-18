// // src/main/java/com/example/BookMyMovie/config/TestWebSocketConfig.java
// package com.example.BookMyMovie.config;

// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Profile;
// import org.springframework.messaging.simp.config.MessageBrokerRegistry;
// import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
// import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
// import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// @Configuration
// @EnableWebSocketMessageBroker
// @Profile("!test") // only active when NOT in test
// public class TestWebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
//     @Override
//     public void configureMessageBroker(MessageBrokerRegistry registry) {
//         // Only simple broker
//         registry.enableSimpleBroker("/topic");
//         registry.setApplicationDestinationPrefixes("/app");
//     }

//     @Override
//     public void registerStompEndpoints(StompEndpointRegistry registry) {
//         registry.addEndpoint("/ws/seats")
//                 .setAllowedOriginPatterns("*")
//                 .withSockJS();
//     }
// }
