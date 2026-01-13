package com.secure.appointment.service;

import com.secure.appointment.dto.response.AppointmentResponse;
import com.secure.appointment.dto.response.TimeSlotResponse;
import com.secure.appointment.entity.Appointment;
import com.secure.appointment.entity.AppointmentStatus;
import com.secure.appointment.entity.TimeSlot;
import com.secure.appointment.entity.User;
import com.secure.appointment.repository.AppointmentRepository;
import com.secure.appointment.repository.TimeSlotRepository;
import com.secure.appointment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, TimeSlotRepository timeSlotRepository, UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AppointmentResponse bookAppointment(Long customerId, Long slotId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        TimeSlot slot = timeSlotRepository.findByIdWithLock(slotId)
                .orElseThrow(() -> new RuntimeException("Time slot not found"));

        // Explicit check before attempting write (fail fast)
        if (slot.isBooked()) {
            throw new RuntimeException("Slot is already booked");
        }

        // Optimistic Locking happens here:
        // If another transaction modified this slot between read and write,
        // JPA will throw ObjectOptimisticLockingFailureException upon transaction
        // commit.
        slot.setBooked(true);
        timeSlotRepository.save(slot);

        Appointment appointment = Appointment.builder()
                .customer(customer)
                .slot(slot)
                .status(AppointmentStatus.BOOKED)
                .bookedAt(LocalDateTime.now())
                .build();

        Appointment savedAppt = appointmentRepository.save(appointment);

        return mapToResponse(savedAppt);
    }

    @Transactional
    public void cancelAppointment(Long customerId, Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Access Denied: You cannot cancel this appointment");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Appointment is already cancelled");
        }

        // Free up the slot
        TimeSlot slot = appointment.getSlot();
        slot.setBooked(false);
        timeSlotRepository.save(slot);

        // Update appointment status
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments(Long customerId) {
        return appointmentRepository.findByCustomerIdOrderByBookedAtDesc(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AppointmentResponse mapToResponse(Appointment appt) {
        return AppointmentResponse.builder()
                .id(appt.getId())
                .bookedAt(appt.getBookedAt())
                .status(appt.getStatus().name())
                .customerName(appt.getCustomer().getName())
                // Re-using TimeSlot mapping logic would be better if extracted to mapper, but
                // doing inline for simplicity
                .slot(TimeSlotResponse.builder()
                        .id(appt.getSlot().getId())
                        .startTime(appt.getSlot().getStartTime())
                        .endTime(appt.getSlot().getEndTime())
                        .providerName(appt.getSlot().getProvider().getName())
                        .isBooked(appt.getSlot().isBooked())
                        .build())
                .build();
    }
}
