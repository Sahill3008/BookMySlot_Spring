package com.secure.appointment.controller;

import com.secure.appointment.dto.response.AppointmentResponse;
import com.secure.appointment.dto.response.MessageResponse;
import com.secure.appointment.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Panel", description = "System-wide Management (Restricted Access)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

	private final AdminService adminService;

	public AdminController(AdminService adminService) {
		this.adminService = adminService;
	}

	@Operation(
		summary = "View All Appointments",
		description = """
			### 1. HUMAN SUMMARY
			Fetches a complete list of EVERY appointment ever made in the system.
			This is a high-level oversight tool for Administrators.
			
			### 2. REAL-WORLD SCENARIO
			The System Admin logs in to check how many bookings are happening across all providers today.
			They view this master list.
			
			### 3. REQUEST EXPLANATION
			- **Source**: No parameters. User must have ROLE_ADMIN.
			
			### 4. AUTH SECTION
			- **Authentication Required**: Yes (Role: ADMIN).
			- **Access Denied**: Providers and Clients will receive 403 Forbidden.
			
			### 5. RESPONSE GUIDE
			- **200 OK**: Full list of appointments.
			"""
	)
	@ApiResponse(responseCode = "200", description = "List retrieved", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AppointmentResponse.class))))
	@GetMapping("/appointments")
	public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
		return ResponseEntity.ok(adminService.getAllAppointments());
	}

	@Operation(
		summary = "Unban/Activate User",
		description = """
			### 1. HUMAN SUMMARY
			Restores access to a user account that was previously banned or deactivated.
			The user will be able to log in again immediately.
			
			### 2. REAL-WORLD SCENARIO
			A user calls support saying they were banned by mistake. The Admin looks up their ID and clicks "Unban".
			
			### 3. REQUEST EXPLANATION
			- **Source**: Path Parameter (`id`) - User ID.
			
			### 4. AUTH SECTION
			- **Authentication Required**: Yes (Role: ADMIN).
			
			### 5. RESPONSE GUIDE
			- **200 OK**: User activated.
			"""
	)
	@ApiResponse(responseCode = "200", description = "User activated", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
	@PutMapping("/users/{id}/activate")
	public ResponseEntity<MessageResponse> activateUser(@PathVariable Long id) {
		adminService.activateUser(id);
		return ResponseEntity.ok(new MessageResponse("User activated successfully"));
	}

	@Operation(
		summary = "Ban/Deactivate User",
		description = """
			### 1. HUMAN SUMMARY
			Suspends a user account.
			The user will effectively be locked out of the system. Their tokens will still appear valid until expired, but the backend will reject requests (depending on security config).
			
			### 2. REAL-WORLD SCENARIO
			An Admin notices a user spamming fake appointments. They click "Ban User".
			
			### 3. REQUEST EXPLANATION
			- **Source**: Path Parameter (`id`) - User ID.
			
			### 4. AUTH SECTION
			- **Authentication Required**: Yes (Role: ADMIN).
			
			### 5. RESPONSE GUIDE
			- **200 OK**: User deactivated.
			"""
	)
	@ApiResponse(responseCode = "200", description = "User deactivated", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
	@PutMapping("/users/{id}/deactivate")
	public ResponseEntity<MessageResponse> deactivateUser(@PathVariable Long id) {
		adminService.deactivateUser(id);
		return ResponseEntity.ok(new MessageResponse("User deactivated successfully"));
	}
}
