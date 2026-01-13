package com.secure.appointment.service;

import com.secure.appointment.dto.request.TimeSlotRequest;
import com.secure.appointment.dto.response.TimeSlotResponse;
import com.secure.appointment.entity.TimeSlot;
import com.secure.appointment.entity.User;
import com.secure.appointment.repository.TimeSlotRepository;
import com.secure.appointment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final com.secure.appointment.repository.AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;

    public TimeSlotService(TimeSlotRepository timeSlotRepository, UserRepository userRepository,
                           com.secure.appointment.repository.AppointmentRepository appointmentRepository,
                           NotificationService notificationService) {
        this.timeSlotRepository = timeSlotRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public TimeSlotResponse createSlot(Long providerId, TimeSlotRequest request) {
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        if (timeSlotRepository.existsByProviderIdAndStartTimeLessThanAndEndTimeGreaterThanAndIsCancelledFalse(
                providerId, request.getEndTime(), request.getStartTime())) {
            throw new IllegalArgumentException("Time slot overlaps with an existing slot");
        }

        TimeSlot slot = TimeSlot.builder()
                .provider(provider)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isBooked(false)
                .isCancelled(false)
                .build();

        TimeSlot savedSlot = timeSlotRepository.save(slot);

        return mapToResponse(savedSlot);
    }

    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getProviderSlots(Long providerId) {
        return timeSlotRepository.findByProviderIdAndIsCancelledFalseOrderByStartTimeAsc(providerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getAvailableSlots() {
        return timeSlotRepository.findByIsBookedFalseAndIsCancelledFalseAndStartTimeAfterOrderByStartTimeAsc(LocalDateTime.now())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelSlot(Long providerId, Long slotId) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Time slot not found"));

        if (!slot.getProvider().getId().equals(providerId)) {
            throw new RuntimeException("You are not authorized to delete this slot");
        }

        if (slot.isCancelled()) {
            throw new RuntimeException("Slot is already cancelled");
        }

        // If booked, cancel the appointment and notify the customer
        if (slot.isBooked()) {
            com.secure.appointment.entity.Appointment appointment = appointmentRepository
                    .findBySlotIdAndStatus(slotId, com.secure.appointment.entity.AppointmentStatus.BOOKED)
                    .orElseThrow(() -> new RuntimeException("Slot is marked booked but no active appointment found"));

            appointment.setStatus(com.secure.appointment.entity.AppointmentStatus.CANCELLED);
            appointment.setCancelledAt(LocalDateTime.now());
            appointmentRepository.save(appointment);

            // Create Notification
            String message = "Your appointment for " + slot.getStartTime().toString() + " has been cancelled by the provider.";
            notificationService.sendNotification(appointment.getCustomer(), message);
        }

        slot.setCancelled(true);
        slot.setBooked(false); // Make sure it's not marked booked anymore
        timeSlotRepository.save(slot);
    }

    private TimeSlotResponse mapToResponse(TimeSlot slot) {
        return TimeSlotResponse.builder()
                .id(slot.getId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .isBooked(slot.isBooked())
                .providerName(slot.getProvider().getName())
                .build();
    }
}
