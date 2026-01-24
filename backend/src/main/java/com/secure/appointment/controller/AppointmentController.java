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

/**
 * AppointmentController: Managing Bookings
 * 
 * What it does: This controller handles the Customer's actions: Booking a slot
 * and Viewing/Cancelling their own appointments.
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

	private final AppointmentService appointmentService;

	public AppointmentController(AppointmentService appointmentService) {
		this.appointmentService = appointmentService;
	}

	/**
	 * Function: bookAppointment
	 * 
	 * 1. FRONTEND TRIGGER: Customer clicks "Book Now" on an available slot.
	 * 
	 * 2. DATA: Receives 'slotId' (Which slot do they want?).
	 * 
	 * 3. LOGIC: Calls appointmentService.bookAppointment() to lock the slot.
	 * 
	 * 4. OUTCOME: A new Appointment is created, and the Slot becomes "Booked".
	 */
	@PostMapping
	public ResponseEntity<AppointmentResponse> bookAppointment(@AuthenticationPrincipal CustomUserDetails userDetails,
			@Valid @RequestBody AppointmentRequest request) {
		return ResponseEntity.ok(appointmentService.bookAppointment(userDetails.getId(), request.getSlotId()));
	}

	/**
	 * Function: getMyAppointments
	 * 
	 * 1. TRIGGER: Customer goes to "My Appointments" page.
	 * 
	 * 2. LOGIC: Finds all appointments for this specific logged-in user.
	 */
	@GetMapping("/my")
	public ResponseEntity<List<AppointmentResponse>> getMyAppointments(
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ResponseEntity.ok(appointmentService.getMyAppointments(userDetails.getId()));
	}

	/**
	 * Function: cancelAppointment
	 * 
	 * 1. TRIGGER: Customer clicks "Cancel" on one of their upcoming appointments.
	 * 
	 * 2. LOGIC: - Verifies ownership. - Frees up the slot (so someone else can take
	 * it). - Marks appointment as Cancelled.
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<?> cancelAppointment(@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable Long id) {
		appointmentService.cancelAppointment(userDetails.getId(), id);
		return ResponseEntity.ok().build();
	}
}
