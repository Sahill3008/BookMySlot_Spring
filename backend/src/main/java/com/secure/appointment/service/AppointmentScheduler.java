package com.secure.appointment.service;

import com.secure.appointment.entity.Appointment;
import com.secure.appointment.entity.AppointmentStatus;
import com.secure.appointment.entity.Notification;
import com.secure.appointment.entity.TimeSlot;
import com.secure.appointment.repository.AppointmentRepository;
import com.secure.appointment.repository.NotificationRepository;
import com.secure.appointment.repository.TimeSlotRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentScheduler {

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final NotificationService notificationService;

    public AppointmentScheduler(AppointmentRepository appointmentRepository, TimeSlotRepository timeSlotRepository, NotificationService notificationService) {
        this.appointmentRepository = appointmentRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.notificationService = notificationService;
    }

    /**
     * Scheduler to cancel appointments based on criteria.
     * Currently checks for PENDING appointments older than 15 minutes.
     */
    @Scheduled(fixedRate = 60000) // Runs every minute
    @Transactional
    public void cancelExpiredAppointments() {
        // Log to show scheduler is active (optional, keep it clean)
        
        // Find tickets pending for more than 15 minutes
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(15);
        List<Appointment> expired = appointmentRepository.findByStatusAndBookedAtBefore(AppointmentStatus.PENDING, expirationTime);

        for (Appointment appt : expired) {
            // 1. Mark appointment as CANCELLED
            appt.setStatus(AppointmentStatus.CANCELLED);
            appt.setCancelledAt(LocalDateTime.now());
            appointmentRepository.save(appt);

            // 2. Free up the TimeSlot
            TimeSlot slot = appt.getSlot();
            slot.setBooked(false);
            // We don't mark slot as cancelled, we just free it for others to book
            timeSlotRepository.save(slot);

            // 3. Send Notification/Alert to User
            String message = "Your pending appointment for " + slot.getStartTime() + " has expired and was cancelled.";
            notificationService.sendNotification(appt.getCustomer(), message);

            System.out.println("Scheduler: Cancelled expired appointment " + appt.getId() + " for user " + appt.getCustomer().getEmail());
        }
    }
}
