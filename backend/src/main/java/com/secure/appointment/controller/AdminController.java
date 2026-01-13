package com.secure.appointment.controller;

import com.secure.appointment.dto.response.AppointmentResponse;
import com.secure.appointment.dto.response.MessageResponse;
import com.secure.appointment.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
        return ResponseEntity.ok(adminService.getAllAppointments());
    }

    @PutMapping("/users/{id}/activate")
    public ResponseEntity<MessageResponse> activateUser(@PathVariable Long id) {
        adminService.activateUser(id);
        return ResponseEntity.ok(new MessageResponse("User activated successfully"));
    }

    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<MessageResponse> deactivateUser(@PathVariable Long id) {
        adminService.deactivateUser(id);
        return ResponseEntity.ok(new MessageResponse("User deactivated successfully"));
    }
}
