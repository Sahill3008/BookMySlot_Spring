package com.secure.appointment.controller;

import com.secure.appointment.dto.request.AppointmentRequest;
import com.secure.appointment.dto.response.AppointmentResponse;
import com.secure.appointment.security.CustomUserDetails;
import com.secure.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.bookAppointment(userDetails.getId(), request.getSlotId()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(appointmentService.getMyAppointments(userDetails.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelAppointment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        appointmentService.cancelAppointment(userDetails.getId(), id);
        return ResponseEntity.ok().build();
    }
}
