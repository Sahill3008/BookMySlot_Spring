package com.secure.appointment.repository;

import com.secure.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // For Customer: My Appointments
    List<Appointment> findByCustomerIdOrderByBookedAtDesc(Long customerId);

    // For Provider: Appointments for my slots
    List<Appointment> findBySlotProviderIdOrderBySlotStartTimeAsc(Long providerId);

    // Find active appointment for a mobile slot
    // Find active appointment for a mobile slot
    java.util.Optional<Appointment> findBySlotIdAndStatus(Long slotId, com.secure.appointment.entity.AppointmentStatus status);

    // Find expired appointments
    List<Appointment> findByStatusAndBookedAtBefore(com.secure.appointment.entity.AppointmentStatus status, java.time.LocalDateTime dateTime);
}
