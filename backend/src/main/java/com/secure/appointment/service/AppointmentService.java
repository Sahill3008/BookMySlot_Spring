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

/**
 * AppointmentService: The Booking Engine
 * 
 * What it does:
 * This handles the complexity of "Booking" a slot.
 * It's responsible for concurrency checks (preventing double booking) and managing appointment lifecycles.
 */
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

    /**
     * Function: bookAppointment
     * 
     * 1. TRIGGER: Called by AppointmentController.bookAppointment()
     * 
     * 2. LOGIC (Critical):
     *    - FETCH: Get the slot from DB. Lock it if possible (Optimistic Locking).
     *    - CHECK: Is it already booked? If yes, ERROR.
     *    - RESERVE: Set slot.isBooked = true.
     *    - CREATE: Save a new Appointment entity linking Customer + Slot.
     * 
     * 3. OUTCOME: Slot is now taken. Appointment ID is returned.
     */
    @Transactional
    public AppointmentResponse bookAppointment(Long customerId, Long slotId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        TimeSlot slot = timeSlotRepository.findByIdWithLock(slotId)
                .orElseThrow(() -> new RuntimeException("Time slot not found"));

        // Check capacity
        if (slot.getBookedCount() >= slot.getCapacity()) {
            throw new RuntimeException("Slot is fully booked");
        }

        // Increment booked count
        slot.setBookedCount(slot.getBookedCount() + 1);

        // Update isBooked flag if capacity reached
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
        
        return com.secure.appointment.util.DtoMapper.toAppointmentResponse(savedAppt);
    }

    /**
     * Function: cancelAppointment
     * 
     * 1. TRIGGER: Customer cancels their own booking.
     * 
     * 2. LOGIC:
     *    - Security: Ensure this appointment belongs to THIS customer.
     *    - Status: Change Appointment status to CANCELLED.
     *    - Slot: Set slot.isBooked = false (So someone else can book it again!).
     * 
     * 3. OUTCOME: Appointment is voided. Slot is free.
     */
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
        slot.setBookedCount(slot.getBookedCount() - 1);
        slot.setBooked(false); // Make sure it's not marked as full anymore
        timeSlotRepository.save(slot);

        // Update appointment status
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
    }

    /**
     * Function: getMyAppointments
     * 
     * 1. TRIGGER: "My Appointments" page load.
     * 
     * 2. LOGIC: Fetch formatted list of appointments.
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments(Long customerId) {
        return appointmentRepository.findByCustomerIdOrderByBookedAtDesc(customerId).stream()
                .map(com.secure.appointment.util.DtoMapper::toAppointmentResponse)
                .collect(Collectors.toList());
    }
}
