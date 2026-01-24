package com.secure.appointment.controller;

import com.secure.appointment.dto.request.TimeSlotRequest;
import com.secure.appointment.dto.response.TimeSlotResponse;
import com.secure.appointment.security.CustomUserDetails;
import com.secure.appointment.service.TimeSlotService;
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
@RequestMapping("/api/provider")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", exposedHeaders = "Content-Disposition", maxAge = 3600)
@Tag(name = "Provider Dashboard", description = "Schedule Management for Doctors/Providers")
@SecurityRequirement(name = "bearerAuth")
public class ProviderController {

    private final TimeSlotService timeSlotService;
    private final com.secure.appointment.service.ReportService reportService;

    public ProviderController(TimeSlotService timeSlotService, com.secure.appointment.service.ReportService reportService) {
        this.timeSlotService = timeSlotService;
        this.reportService = reportService;
    }

    @Operation(
        summary = "Create Availability Slot",
        description = """
            ### 1. HUMAN SUMMARY
            Adds a new block of time when the provider is available to see patients.
            Providers use this to build their daily schedule.
            
            ### 2. REAL-WORLD SCENARIO
            Dr. Smith decides she wants to work next Monday from 9 AM to 5 PM.
            She adds multiple 1-hour slots: 9-10, 10-11, etc. This endpoint creates one of those slots.
            
            ### 3. REQUEST EXPLANATION
            - **Source**: Request Body (JSON)
            - **Required**: Yes
            - **Fields**:
                - `startTime`: ISO-8601 DateTime string (e.g., "2025-10-25T09:00:00").
                - `endTime`: ISO-8601 DateTime string (e.g., "2025-10-25T10:00:00").
            
            ### 4. AUTH SECTION
            - **Authentication Required**: Yes (Role: PROVIDER).
            
            ### 5. RESPONSE GUIDE
            - **200 OK**: Slot created successfully. Returns the created object.
            - **400 Bad Request**: 
                - Start time is in the past.
                - End time is before Start time.
                - Slot overlaps with an existing one.
            
            ### 6. ERROR DIAGNOSIS
            - **400 Bad Request**: "Time slot overlaps". You already have a slot at this time. Check your existing schedule.
            """
    )
    @ApiResponse(responseCode = "200", description = "Slot created", content = @Content(schema = @Schema(implementation = TimeSlotResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid time or overlap")
    @PostMapping("/slots")
    public ResponseEntity<TimeSlotResponse> createSlot(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TimeSlotRequest request) {
        return ResponseEntity.ok(timeSlotService.createSlot(userDetails.getId(), request));
    }

    @Operation(
        summary = "Get My Schedule",
        description = """
            ### 1. HUMAN SUMMARY
            Retrieves all time slots (booked, available, or cancelled) created by the logged-in provider.
            
            ### 2. REAL-WORLD SCENARIO
            Dr. Smith logs in to see her agenda for the week. The dashboard loads this list to verify which slots are taken.
            
            ### 3. REQUEST EXPLANATION
            - **Source**: No parameters. User ID from Token.
            
            ### 4. AUTH SECTION
            - **Authentication Required**: Yes (Role: PROVIDER).
            
            ### 5. RESPONSE GUIDE
            - **200 OK**: List of slots.
            """
    )
    @ApiResponse(responseCode = "200", description = "Schedule retrieved", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TimeSlotResponse.class))))
    @GetMapping("/slots")
    public ResponseEntity<List<TimeSlotResponse>> getMySlots(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(timeSlotService.getProviderSlots(userDetails.getId()));
    }

    @Operation(
        summary = "Delete/Cancel a Slot",
        description = """
            ### 1. HUMAN SUMMARY
            Removes an availability slot. 
            **WARNING**: If this slot was already booked by a patient, the appointment is automatically CANCELLED and the patient is notified.
            
            ### 2. REAL-WORLD SCENARIO
            Dr. Smith has an emergency and can't work on Tuesday at 10 AM. She deletes the slot.
            The system removes it and emails the patient who booked it: "Your appointment has been cancelled by the provider."
            
            ### 3. REQUEST EXPLANATION
            - **Source**: Path Parameter (`id`)
            - **Meaning**: The Unique ID of the Slot.
            
            ### 4. AUTH SECTION
            - **Authentication Required**: Yes.
            
            ### 5. RESPONSE GUIDE
            - **200 OK**: Slot deleted.
            - **404 Not Found**: Slot ID does not exist.
            - **403 Forbidden**: You tried to delete someone else's slot.
            """
    )
    @ApiResponse(responseCode = "200", description = "Slot deleted")
    @DeleteMapping("/slots/{id}")
    public ResponseEntity<?> deleteSlot(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        timeSlotService.cancelSlot(userDetails.getId(), id);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Update Slot Capacity (Optional)",
        description = """
            ### 1. HUMAN SUMMARY
            Changes how many people can book a single slot (if group appointments are allowed).
            Usually capacity is 1 (one-on-one).
            
            ### 2. REAL-WORLD SCENARIO
            A Yoga Class slot can hold 10 people. The instructor increases capacity from 10 to 12.
            """
    )
    @PutMapping("/slots/{id}/capacity")
    public ResponseEntity<TimeSlotResponse> updateCapacity(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Integer> payload) {
        Integer newCapacity = payload.get("capacity");
        if (newCapacity == null || newCapacity < 1) {
            throw new IllegalArgumentException("Capacity must be at least 1");
        }
        return ResponseEntity.ok(timeSlotService.updateSlotCapacity(userDetails.getId(), id, newCapacity));
    }
    
    @Operation(
        summary = "Export Appointment Report (Excel)",
        description = """
            ### 1. HUMAN SUMMARY
            Downloads an Excel file (.xlsx) containing details of the appointment for a specific slot.
            
            ### 2. REAL-WORLD SCENARIO
            Dr. Smith wants a printed list (or offline copy) of who is coming at 10 AM. She clicks "Export".
            A file named `Appointment_2025-10-25_10-00_to_11-00.xlsx` downloads.
            
            ### 3. REQUEST EXPLANATION
            - **Source**: Path Parameter (`id`) - The ID of the Slot (not the appointment).
            
            ### 4. AUTH SECTION
            - **Authentication Required**: Yes (Role: PROVIDER).
            
            ### 5. RESPONSE GUIDE
            - **200 OK**: Binary file download (Excel).
            """
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Excel file download", 
        content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    )
    @GetMapping("/slots/{id}/report")
    public ResponseEntity<org.springframework.core.io.InputStreamResource> exportAppointments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) throws java.io.IOException {
        
        // Fetch slot details to generate filename
        TimeSlotResponse slot = timeSlotService.getSlotById(id);

        java.io.ByteArrayInputStream in = reportService.generateAppointmentReport(id);
        
        // Format: Appointment_YYYY-MM-DD_HH-MM_to_HH-MM.xlsx
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
        String filename = String.format("Appointment_%s_to_%s.xlsx", 
                slot.getStartTime().format(formatter), 
                slot.getEndTime().format(formatter));
        
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + filename);
        headers.add("Access-Control-Expose-Headers", "Content-Disposition");
        
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new org.springframework.core.io.InputStreamResource(in));
    }
}
