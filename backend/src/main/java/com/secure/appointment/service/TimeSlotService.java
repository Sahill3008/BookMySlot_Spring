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
    private final EmailService emailService;

    public TimeSlotService(TimeSlotRepository timeSlotRepository, UserRepository userRepository,
                           com.secure.appointment.repository.AppointmentRepository appointmentRepository,
                           NotificationService notificationService,
                           EmailService emailService) {
        this.timeSlotRepository = timeSlotRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
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

        java.util.Optional<TimeSlot> existingSlotOpt = timeSlotRepository.findByProviderIdAndStartTime(providerId, request.getStartTime());

        TimeSlot savedSlot;
        if (existingSlotOpt.isPresent()) {
            TimeSlot existingSlot = existingSlotOpt.get();
            if (!existingSlot.isCancelled()) {
                throw new IllegalArgumentException("Time slot already exists");
            }
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

    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getProviderSlots(Long providerId) {
        return timeSlotRepository.findByProviderIdAndIsCancelledFalseOrderByStartTimeAsc(providerId).stream()
                .map(com.secure.appointment.util.DtoMapper::toTimeSlotResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TimeSlotResponse getSlotById(Long slotId) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));
        return com.secure.appointment.util.DtoMapper.toTimeSlotResponse(slot);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<TimeSlotResponse> getAvailableSlots(org.springframework.data.domain.Pageable pageable) {
        return timeSlotRepository.findByIsBookedFalseAndIsCancelledFalseAndStartTimeAfter(LocalDateTime.now(), pageable)
                .map(com.secure.appointment.util.DtoMapper::toTimeSlotResponse);
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

        List<com.secure.appointment.entity.Appointment> appointments = appointmentRepository
                .findAllBySlotIdAndStatus(slotId, com.secure.appointment.entity.AppointmentStatus.BOOKED);

        for (com.secure.appointment.entity.Appointment appointment : appointments) {
            appointment.setStatus(com.secure.appointment.entity.AppointmentStatus.CANCELLED);
            appointment.setCancelledAt(LocalDateTime.now());
            appointmentRepository.save(appointment);

            String message = "Your appointment for " + slot.getStartTime().toString() + " has been cancelled by the provider.";
            notificationService.sendNotification(appointment.getCustomer(), message);
            
            String subject = "Important: Appointment Cancelled by Provider";
            String body = String.format("Dear %s,\n\nRegrettably, your appointment for %s has been cancelled by the provider.\n\nPlease check the portal to book a new slot.\n\nApologies for the inconvenience.", 
                    appointment.getCustomer().getEmail(), 
                    slot.getStartTime());
            emailService.sendEmail(appointment.getCustomer().getEmail(), subject, body);
        }

        slot.setCancelled(true);
        slot.setBooked(false); 
        slot.setBookedCount(0); 
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

        if (slot.getBookedCount() >= newCapacity) {
            slot.setBooked(true);
        } else {
            slot.setBooked(false);
        }

        TimeSlot savedSlot = timeSlotRepository.save(slot);
        return com.secure.appointment.util.DtoMapper.toTimeSlotResponse(savedSlot);
    }
}
