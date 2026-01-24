package com.secure.appointment.service;

import com.secure.appointment.dto.response.AppointmentResponse;
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
    private final EmailService emailService;

    public AppointmentService(AppointmentRepository appointmentRepository, 
                              TimeSlotRepository timeSlotRepository, 
                              UserRepository userRepository,
                              EmailService emailService) {
        this.appointmentRepository = appointmentRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public AppointmentResponse bookAppointment(Long customerId, Long slotId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        TimeSlot slot = timeSlotRepository.findByIdWithLock(slotId)
                .orElseThrow(() -> new RuntimeException("Time slot not found"));

        if (slot.getBookedCount() >= slot.getCapacity()) {
            throw new RuntimeException("Slot is fully booked");
        }

        slot.setBookedCount(slot.getBookedCount() + 1);

        if (slot.getBookedCount() >= slot.getCapacity()) {
            slot.setBooked(true);
        }
        
        timeSlotRepository.save(slot);

        Appointment appointment = Appointment.builder()
                .customer(customer)
                .slot(slot)
                .status(AppointmentStatus.BOOKED)
                .bookedAt(LocalDateTime.now())
                .build();

        Appointment savedAppt = appointmentRepository.save(appointment);
        
        String subject = "Appointment Confirmation - BookMySlot";
        String body = String.format("Dear %s,\n\nYour appointment with %s is confirmed for %s.\n\nThank you for choosing BookMySlot.", 
                customer.getEmail(), 
                slot.getProvider().getEmail(), 
                slot.getStartTime());
        emailService.sendEmail(customer.getEmail(), subject, body);

        return com.secure.appointment.util.DtoMapper.toAppointmentResponse(savedAppt);
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

        TimeSlot slot = appointment.getSlot();
        slot.setBookedCount(slot.getBookedCount() - 1);
        slot.setBooked(false); 
        timeSlotRepository.save(slot);

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
        
        String subject = "Appointment Cancellation - BookMySlot";
        String body = String.format("Dear %s,\n\nYour appointment for %s has been cancelled successfully.\n\nRegards,\nBookMySlot Team", 
                appointment.getCustomer().getEmail(), 
                slot.getStartTime());
        emailService.sendEmail(appointment.getCustomer().getEmail(), subject, body);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments(Long customerId) {
        return appointmentRepository.findByCustomerIdOrderByBookedAtDesc(customerId).stream()
                .map(com.secure.appointment.util.DtoMapper::toAppointmentResponse)
                .collect(Collectors.toList());
    }
}
