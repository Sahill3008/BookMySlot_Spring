package com.secure.appointment.controller;

import com.secure.appointment.dto.response.NotificationResponse;
import com.secure.appointment.entity.Notification;
import com.secure.appointment.repository.NotificationRepository;
import com.secure.appointment.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Notifications", description = "User Alerts and Messages")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Operation(
        summary = "Fetch Unread Notifications",
        description = """
            ### 1. HUMAN SUMMARY
            Retrieves all *unread* alerts for the logged-in user.
            These messages usually inform about appointment bookings, cancellations, or reminders.
            
            ### 2. REAL-WORLD SCENARIO
            Sarah logs in after a weekend. She sees a bell icon with a "1" badge. 
            The app calls this endpoint to find out what that message is (e.g., "Your appointment was confirmed").
            
            ### 3. REQUEST EXPLANATION
            - **Source**: No parameters. User ID extracted from token.
            
            ### 4. AUTH SECTION
            - **Authentication Required**: Yes.
            
            ### 5. RESPONSE GUIDE
            - **200 OK**: List of Notification objects.
                - `id`: ID of the message.
                - `message`: Text content.
                - `read`: false.
                - `timestamp`: When it was sent.
            """
    )
    @ApiResponse(responseCode = "200", description = "Notifications retrieved", content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificationResponse.class))))
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Notification> notifications = notificationRepository
                .findByRecipientIdAndReadFalseOrderByCreatedAtDesc(userDetails.getId());
        
        List<NotificationResponse> response = notifications.stream()
                .map(n -> new NotificationResponse(
                        n.getId(),
                        n.getMessage(),
                        n.isRead(),
                        n.getCreatedAt()
                ))
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Mark Notification as Read",
        description = """
            ### 1. HUMAN SUMMARY
            Tells the system "I have seen this message".
            It updates the status to `read = true`, so it won't appear in the "Unread" list again.
            
            ### 2. REAL-WORLD SCENARIO
            Sarah clicks the "X" or "Dismiss" button on the notification toast.
            The app sends this request to ensure she doesn't see the same alert 5 minutes later.
            
            ### 3. REQUEST EXPLANATION
            - **Source**: Path Parameter (`id`) - Notification ID.
            
            ### 4. AUTH SECTION
            - **Authentication Required**: Yes.
            - **Validation**: You can only dismiss *your own* notifications.
            
            ### 5. RESPONSE GUIDE
            - **200 OK**: Marked as read.
            - **403 Forbidden**: You tried to dismiss someone else's notification.
            """
    )
    @ApiResponse(responseCode = "200", description = "Marked as read")
    @ApiResponse(responseCode = "403", description = "Forbidden access to notification")
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
