package com.secure.appointment.service;

import com.secure.appointment.entity.Appointment;
import com.secure.appointment.entity.AppointmentStatus;
import com.secure.appointment.entity.TimeSlot;
import com.secure.appointment.repository.AppointmentRepository;
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

    @Scheduled(fixedRate = 60000) 
    @Transactional
    public void cancelExpiredAppointments() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(15);
        List<Appointment> expired = appointmentRepository.findByStatusAndBookedAtBefore(AppointmentStatus.PENDING, expirationTime);

        for (Appointment appt : expired) {
            appt.setStatus(AppointmentStatus.CANCELLED);
            appt.setCancelledAt(LocalDateTime.now());
            appointmentRepository.save(appt);

            TimeSlot slot = appt.getSlot();
            slot.setBooked(false);
            timeSlotRepository.save(slot);

            String message = "Your pending appointment for " + slot.getStartTime() + " has expired and was cancelled.";
            notificationService.sendNotification(appt.getCustomer(), message);

            System.out.println("Scheduler: Cancelled expired appointment " + appt.getId() + " for user " + appt.getCustomer().getEmail());
        }
    }
}
