package com.secure.appointment.service;

import com.secure.appointment.dto.response.AppointmentResponse;
import com.secure.appointment.dto.response.TimeSlotResponse;
import com.secure.appointment.entity.User;
import com.secure.appointment.repository.AppointmentRepository;
import com.secure.appointment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AdminService: Administrative Logic
 * 
 * What it does:
 * Handles high-level system operations like:
 * - Viewing all system data (Global View).
 * - User Management (Ban/Unban).
 */
@Service
public class AdminService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public AdminService(AppointmentRepository appointmentRepository, UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Function: getAllAppointments
     * 
     * 1. TRIGGER: Admin Dashboard.
     * 
     * 2. LOGIC: findAll() fetches the entire table.
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(com.secure.appointment.util.DtoMapper::toAppointmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Function: activateUser
     * 
     * 1. TRIGGER: Admin Action.
     * 
     * 2. LOGIC: Finds user and toggles their active flag.
     *    This flag is checked during Login in 'loadUserByUsername'.
     */
    @Transactional
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(true);
        userRepository.save(user);
    }

    /**
     * Function: deactivateUser
     * 
     * 1. TRIGGER: Admin Action.
     * 
     * 2. LOGIC: Sets user to inactive.
     */
    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }
}
