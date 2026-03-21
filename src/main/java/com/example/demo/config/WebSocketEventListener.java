package com.example.demo.config;

import com.example.demo.service.UserStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    @Autowired
    private UserStatusService userStatusService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        try {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
            // Read from native STOMP headers
            String username = accessor.getFirstNativeHeader("username");
            if (username != null && !username.isEmpty()) {
                userStatusService.setOnline(username);
                messagingTemplate.convertAndSend("/topic/status",
                    userStatusService.getOnlineUsers());
            }
        } catch (Exception e) {
            System.out.println("Connect event error: " + e.getMessage());
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        try {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
            // Read from native STOMP headers
            String username = accessor.getFirstNativeHeader("username");
            if (username != null && !username.isEmpty()) {
                userStatusService.setOffline(username);
                messagingTemplate.convertAndSend("/topic/status",
                    userStatusService.getOnlineUsers());
            }
        } catch (Exception e) {
            System.out.println("Disconnect event error: " + e.getMessage());
        }
    }
}