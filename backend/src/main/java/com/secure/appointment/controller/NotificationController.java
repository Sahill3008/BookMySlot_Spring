package com.secure.appointment.controller;

import com.secure.appointment.entity.Notification;
import com.secure.appointment.repository.NotificationRepository;
import com.secure.appointment.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * NotificationController: Inbox for Alerts
 * 
 * What it does:
 * This controller handles fetching missed notifications and marking them as read.
 * While real-time alerts go through WebSockets, this API helps when the user first logs in
 * or refreshes the page.
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Function: getMyNotifications
     * 
     * 1. TRIGGER: User logs in or refreshes the page (NotificationListener.jsx mount).
     * 
     * 2. LOGIC: 
     *    - Fetch all database records where recipient = current user AND isRead = false.
     *    - Returns them so the UI can show them immediately if they were missed while offline.
     * 
     * 3. OUTCOME: A list of unread messages.
     */
    @GetMapping
    public ResponseEntity<List<com.secure.appointment.dto.response.NotificationResponse>> getMyNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Notification> notifications = notificationRepository
                .findByRecipientIdAndReadFalseOrderByCreatedAtDesc(userDetails.getId());
        
        List<com.secure.appointment.dto.response.NotificationResponse> response = notifications.stream()
                .map(n -> new com.secure.appointment.dto.response.NotificationResponse(
                        n.getId(),
                        n.getMessage(),
                        n.isRead(),
                        n.getCreatedAt()
                ))
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Function: markAsRead
     * 
     * 1. TRIGGER: Frontend displays a persistent notification toast.
     * 
     * 2. LOGIC:
     *    - Finds the notification.
     *    - Security: Keeps users from hacking/reading others' messages.
     *    - Updates 'isRead' to true in the database.
     * 
     * 3. OUTCOME: The notification won't appear again on next refresh.
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getRecipient().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).build();
        }

        notification.setRead(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok().build();
    }
}
