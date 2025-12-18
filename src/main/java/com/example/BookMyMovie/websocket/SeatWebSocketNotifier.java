// package com.example.BookMyMovie.websocket;


// import com.example.BookMyMovie.dtos.events.SeatEvent;
// import lombok.RequiredArgsConstructor;
// import org.springframework.messaging.simp.SimpMessagingTemplate;
// import org.springframework.stereotype.Component;

// @Component
// @RequiredArgsConstructor
// public class SeatWebSocketNotifier {

//     private final SimpMessagingTemplate messagingTemplate;

//     // destination: /topic/shows/{showId}
//     public void broadcastSeatEvent(SeatEvent event) {
//         String dest = "/topic/shows/" + event.getShowId();
//         messagingTemplate.convertAndSend(dest, event);
//     }
// }
