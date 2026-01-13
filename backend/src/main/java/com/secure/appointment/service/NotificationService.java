package com.secure.appointment.service;

import com.secure.appointment.dto.response.NotificationResponse;
import com.secure.appointment.entity.Notification;
import com.secure.appointment.entity.User;
import com.secure.appointment.repository.NotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository, SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void sendNotification(User recipient, String message) {
        // 1. Save to Database
        Notification notification = new Notification(recipient, message);
        Notification saved = notificationRepository.save(notification);

        // 2. Push to WebSocket (Real-time)
        // We push to a specific user's queue: /user/{email}/queue/notifications
        // Note: Spring Security integration allows convertAndSendToUser to direct messages
        // to a specific authenticated user.
        
        NotificationResponse response = new NotificationResponse(
                saved.getId(),
                saved.getMessage(),
                saved.isRead(),
                saved.getCreatedAt()
        );

        // convertAndSendToUser automatically prefixes with "/user" and uses the principal name (email)
        // Destination becomes: /user/{email}/queue/notifications
        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(), 
                "/queue/notifications", 
                response
        );
        
        System.out.println("Notification sent to " + recipient.getEmail() + ": " + message);
    }
}
