package com.secure.appointment.controller;

import com.secure.appointment.dto.response.AppointmentResponse;
import com.secure.appointment.dto.response.MessageResponse;
import com.secure.appointment.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AdminController: System Management
 * 
 * What it does: This controller is for the "Super User" (Administrator). It can
 * see everything and ban/unban users. Protected by ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private final AdminService adminService;

	public AdminController(AdminService adminService) {
		this.adminService = adminService;
	}

	/**
	 * Function: getAllAppointments
	 * 
	 * 1. TRIGGER: Admin Dashboard loads.
	 * 
	 * 2. LOGIC: Fetches EVERY appointment in the system (for analytics/oversight).
	 */
	@GetMapping("/appointments")
	public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
		return ResponseEntity.ok(adminService.getAllAppointments());
	}

	/**
	 * Function: activateUser
	 * 
	 * 1. TRIGGER: Admin clicks "Unban" on a user.
	 * 
	 * 2. LOGIC: Sets user.isActive = true.
	 */
	@PutMapping("/users/{id}/activate")
	public ResponseEntity<MessageResponse> activateUser(@PathVariable Long id) {
		adminService.activateUser(id);
		return ResponseEntity.ok(new MessageResponse("User activated successfully"));
	}

	/**
	 * Function: deactivateUser
	 * 
	 * 1. TRIGGER: Admin clicks "Ban" on a user.
	 * 
	 * 2. LOGIC: - Sets user.isActive = false. - This prevents them from logging in
	 * (checked in CustomUserDetailsService).
	 */
	@PutMapping("/users/{id}/deactivate")
	public ResponseEntity<MessageResponse> deactivateUser(@PathVariable Long id) {
		adminService.deactivateUser(id);
		return ResponseEntity.ok(new MessageResponse("User deactivated successfully"));
	}
}
