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

@Service
public class AdminService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public AdminService(AppointmentRepository appointmentRepository, UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(appt -> AppointmentResponse.builder()
                        .id(appt.getId())
                        .bookedAt(appt.getBookedAt())
                        .status(appt.getStatus().name())
                        .customerName(appt.getCustomer().getName())
                        .slot(TimeSlotResponse.builder()
                                .id(appt.getSlot().getId())
                                .startTime(appt.getSlot().getStartTime())
                                .endTime(appt.getSlot().getEndTime())
                                .providerName(appt.getSlot().getProvider().getName())
                                .isBooked(appt.getSlot().isBooked())
                                .build())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(true);
        userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }
}
