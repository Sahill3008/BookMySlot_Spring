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

/**
 * TimeSlotService: Managing Provider Availability
 * 
 * What it does:
 * This service handles everything related to "Time Slots".
 * - Creating new slots (Providers).
 * - Listing available slots (Customers).
 * - Cancelling slots.
 */
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

    /**
     * Function: createSlot
     * 
     * 1. FRONTEND TRIGGER: Provider clicks "Add Slot" on Dashboard.
     * 
     * 2. LOGIC:
     *    - Validates that Start Time is before End Time.
     *    - Checks for OVERLAPS: A provider cannot work two jobs at the same time!
     *      (It ignores previously cancelled slots).
     *    - Saves a new TimeSlot to the database with isBooked = false.
     * 
     * 3. OUTCOME: New slot appears in the Provider's list and Customer's search list.
     */
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

        // Check if a slot with EXACT start time exists (active or cancelled) to avoid Unique Constraint violation
        java.util.Optional<TimeSlot> existingSlotOpt = timeSlotRepository.findByProviderIdAndStartTime(providerId, request.getStartTime());

        TimeSlot savedSlot;
        if (existingSlotOpt.isPresent()) {
            TimeSlot existingSlot = existingSlotOpt.get();
            if (!existingSlot.isCancelled()) {
                throw new IllegalArgumentException("Time slot already exists");
            }
            // Reactivate the cancelled slot
            existingSlot.setEndTime(request.getEndTime());
            existingSlot.setCancelled(false);
            existingSlot.setBooked(false);
            existingSlot.setCapacity(request.getCapacity() != null ? request.getCapacity() : 1);
            existingSlot.setBookedCount(0);
            savedSlot = timeSlotRepository.save(existingSlot);
        } else {
            TimeSlot slot = TimeSlot.builder()
                    .provider(provider)
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .isBooked(false)
                    .capacity(request.getCapacity() != null ? request.getCapacity() : 1)
                    .bookedCount(0)
                    .isCancelled(false)
                    .build();
            savedSlot = timeSlotRepository.save(slot);
        }

        return com.secure.appointment.util.DtoMapper.toTimeSlotResponse(savedSlot);
    }

    /**
     * Function: getProviderSlots
     * 
     * 1. FRONTEND TRIGGER: Provider logs in and views their Dashboard.
     * 
     * 2. LOGIC: Fetch all slots for this provider that are NOT cancelled.
     */
    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getProviderSlots(Long providerId) {
        return timeSlotRepository.findByProviderIdAndIsCancelledFalseOrderByStartTimeAsc(providerId).stream()
                .map(com.secure.appointment.util.DtoMapper::toTimeSlotResponse)
                .collect(Collectors.toList());
    }

    /**
     * Function: getAvailableSlots
     * 
     * 1. FRONTEND TRIGGER: Customer lands on the Home Page.
     * 
     * 2. LOGIC:
     *    - Fetch slots that are:
     *      a. Not Booked (isBooked = false)
     *      b. Not Cancelled (isCancelled = false)
     *      c. In the Future (startTime > now)
     */
    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getAvailableSlots() {
        return timeSlotRepository.findByIsBookedFalseAndIsCancelledFalseAndStartTimeAfterOrderByStartTimeAsc(LocalDateTime.now())
                .stream()
                .map(com.secure.appointment.util.DtoMapper::toTimeSlotResponse)
                .collect(Collectors.toList());
    }

    /**
     * Function: cancelSlot
     * 
     * 1. FRONTEND TRIGGER: Provider clicks "Delete" on a slot.
     * 
     * 2. LOGIC:
     *    - Security Check: Is this YOUR slot?
     *    - IF BOOKED: 
     *      a. Find the appointment.
     *      b. Mark appointment as CANCELLED.
     *      c. SEND NOTIFICATION to the Customer (via WebSocket).
     *    - Mark slot as isCancelled = true.
     * 
     * 3. OUTCOME: Slot disappears from public view. Customer gets an alert.
     */
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
        // Fetch all active appointments for this slot (could be multiple if capacity > 1)
        List<com.secure.appointment.entity.Appointment> appointments = appointmentRepository
                .findAllBySlotIdAndStatus(slotId, com.secure.appointment.entity.AppointmentStatus.BOOKED);

        for (com.secure.appointment.entity.Appointment appointment : appointments) {
            appointment.setStatus(com.secure.appointment.entity.AppointmentStatus.CANCELLED);
            appointment.setCancelledAt(LocalDateTime.now());
            appointmentRepository.save(appointment);

            // Create Notification
            String message = "Your appointment for " + slot.getStartTime().toString() + " has been cancelled by the provider.";
            notificationService.sendNotification(appointment.getCustomer(), message);
        }

        slot.setCancelled(true);
        slot.setBooked(false); // Make sure it's not marked booked anymore
        slot.setBookedCount(0); // Reset booking count
        timeSlotRepository.save(slot);
    }

    @Transactional
    public TimeSlotResponse updateSlotCapacity(Long providerId, Long slotId, int newCapacity) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Time slot not found"));

        if (!slot.getProvider().getId().equals(providerId)) {
            throw new RuntimeException("You are not authorized to update this slot");
        }

        if (slot.isCancelled()) {
            throw new RuntimeException("Cannot update a cancelled slot");
        }

        if (newCapacity < slot.getBookedCount()) {
            throw new IllegalArgumentException("Cannot decrease capacity below current booked count (" + slot.getBookedCount() + ")");
        }

        slot.setCapacity(newCapacity);

        // Update isBooked status based on new capacity
        if (slot.getBookedCount() >= newCapacity) {
            slot.setBooked(true);
        } else {
            // If we increased capacity, it might not be full anymore
            slot.setBooked(false);
        }

        TimeSlot savedSlot = timeSlotRepository.save(slot);
        return com.secure.appointment.util.DtoMapper.toTimeSlotResponse(savedSlot);
    }
}
