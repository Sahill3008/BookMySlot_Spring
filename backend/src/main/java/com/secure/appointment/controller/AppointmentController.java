package com.secure.appointment.controller;

import com.secure.appointment.dto.request.AppointmentRequest;
import com.secure.appointment.dto.response.AppointmentResponse;
import com.secure.appointment.security.CustomUserDetails;
import com.secure.appointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@Tag(name = "Appointments", description = "Managing Bookings and Cancellations")
@SecurityRequirement(name = "bearerAuth") 
public class AppointmentController {

	private final AppointmentService appointmentService;

	public AppointmentController(AppointmentService appointmentService) {
		this.appointmentService = appointmentService;
	}

	@Operation(
		summary = "Book an Appointment",
		description = """
			### 1. HUMAN SUMMARY
			This endpoint is used by Clients/Patients to reserve a specific time slot with a provider. 
			It effectively "locks" the slot so no one else can take it.
			
			### 2. REAL-WORLD SCENARIO
			After browsing Dr. Smith's available times, Sarah sees a slot for "10:00 AM" (ID: 123) that matches her schedule.
			She clicks "Book Now". The backend receives this request, checks if slot #123 is still free, and confirms the booking.
			
			### 3. REQUEST EXPLANATION
			- **Source**: Request Body (JSON)
			- **Required**: Yes
			- **Fields**:
				- `slotId`: The unique ID of the time slot you want to book (e.g., 55).
			
			### 4. AUTH SECTION
			- **Authentication Required**: Yes (Bearer Token).
			- **Header**: `Authorization: Bearer <your_jwt_token>`
			- **Missing/Expired**: Returns 401 Unauthorized. You must log in first.
			
			### 5. RESPONSE GUIDE
			- **200 OK**: Booking successful.
				- Returns an Appointment object with status "BOOKED".
			- **409 Conflict**: The slot was just taken by someone else 1 millisecond ago (Race Condition).
			- **404 Not Found**: The slot ID provided does not exist.
			
			### 6. ERROR DIAGNOSIS
			- **409 Conflict**: "Slot is no longer available". This is common in high-traffic systems. Ask user to choose another time.
			"""
	)
	@ApiResponse(responseCode = "200", description = "Booking successful", content = @Content(schema = @Schema(implementation = AppointmentResponse.class)))
	@ApiResponse(responseCode = "404", description = "Slot not found")
	@ApiResponse(responseCode = "409", description = "Slot already booked")
	@PostMapping
	public ResponseEntity<AppointmentResponse> bookAppointment(@AuthenticationPrincipal CustomUserDetails userDetails,
			@Valid @RequestBody AppointmentRequest request) {
		return ResponseEntity.ok(appointmentService.bookAppointment(userDetails.getId(), request.getSlotId()));
	}

	@Operation(
		summary = "View My Appointments",
		description = """
			### 1. HUMAN SUMMARY
			Fetches a history of all appointments made by the *currently logged-in user*.
			It filters data so users only see their own bookings, not others'.
			
			### 2. REAL-WORLD SCENARIO
			Sarah wants to check when her next doctor's visit is. She navigates to the "My Bookings" tab.
			The app calls this endpoint, and the server says: "You have a booking with Dr. Smith on Monday at 10 AM".
			
			### 3. REQUEST EXPLANATION
			- **Source**: No parameters required. The user identity is extracted entirely from the Auth Token.
			
			### 4. AUTH SECTION
			- **Authentication Required**: Yes.
			
			### 5. RESPONSE GUIDE
			- **200 OK**: Returns a JSON Array of Appointment objects.
				- `[]` (Empty List): You have no bookings yet.
			
			### 6. ERROR DIAGNOSIS
			- **403 Forbidden**: If your token is valid but you don't have permission (unlikely for this endpoint unless account is banned).
			"""
	)
	@ApiResponse(responseCode = "200", description = "List retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AppointmentResponse.class))))
	@GetMapping("/my")
	public ResponseEntity<List<AppointmentResponse>> getMyAppointments(
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ResponseEntity.ok(appointmentService.getMyAppointments(userDetails.getId()));
	}

	@Operation(
		summary = "Cancel an Appointment",
		description = """
			### 1. HUMAN SUMMARY
			Cancels an existing booking. This frees up the time slot so other users can book it again.
			
			### 2. REAL-WORLD SCENARIO
			Sarah realizes she cannot make it to the 10 AM appointment. She clicks "Cancel".
			The system removes her claim on the slot and notifies the provider.
			
			### 3. REQUEST EXPLANATION
			- **Source**: Path Parameter (`id`)
			- **Required**: Yes
			- **Meaning**: The Unique ID of the *Appointment* (NOT the slot ID) to cancel.
			
			### 4. AUTH SECTION
			- **Authentication Required**: Yes.
			- **Validation**: You can only cancel *your own* appointments. Trying to cancel someone else's will fail.
			
			### 5. RESPONSE GUIDE
			- **200 OK**: Cancellation successful. No content returned.
			- **404 Not Found**: Appointment ID does not exist.
			- **403 Forbidden**: You tried to cancel an appointment that belongs to another user.
			
			### 6. ERROR DIAGNOSIS
			- **403 Forbidden**: "You are not authorized to cancel this appointment". Ensure you are using the correct Appointment ID from the list.
			"""
	)
	@ApiResponse(responseCode = "200", description = "Cancellation successful")
	@ApiResponse(responseCode = "403", description = "Not authorized to cancel this appointment")
	@ApiResponse(responseCode = "404", description = "Appointment not found")
	@DeleteMapping("/{id}")
	public ResponseEntity<?> cancelAppointment(@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable Long id) {
		appointmentService.cancelAppointment(userDetails.getId(), id);
		return ResponseEntity.ok().build();
	}
}
