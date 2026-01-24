package com.secure.appointment.controller;

import com.secure.appointment.dto.request.TimeSlotRequest;
import com.secure.appointment.dto.response.TimeSlotResponse;
import com.secure.appointment.security.CustomUserDetails;
import com.secure.appointment.service.TimeSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ProviderController: Review and Manage Work Schedule
 * 
 * What it does:
 * This controller allows "Providers" (e.g., Doctors, Consultants) to manage their daily schedules.
 * Only users with ROLE_PROVIDER can access these endpoints.
 */
@RestController
@RequestMapping("/api/provider")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", exposedHeaders = "Content-Disposition", maxAge = 3600)
public class ProviderController {

    private final TimeSlotService timeSlotService;
    private final com.secure.appointment.service.ReportService reportService;

    public ProviderController(TimeSlotService timeSlotService, com.secure.appointment.service.ReportService reportService) {
        this.timeSlotService = timeSlotService;
        this.reportService = reportService;
    }

    /**
     * Function: createSlot (Add new availability)
     * 
     * 1. TRIGGER: Provider clicks "Add Slot" button on the Dashboard.
     * 
     * 2. DATA: Receives Start Time and End Time (e.g., 10:00 AM - 11:00 AM).
     * 
     * 3. LOGIC: Calls timeSlotService.createSlot() to validate and save.
     * 
     * 4. OUTCOME: A new slot is added to the database.
     */
    @PostMapping("/slots")
    public ResponseEntity<TimeSlotResponse> createSlot(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TimeSlotRequest request) {
        return ResponseEntity.ok(timeSlotService.createSlot(userDetails.getId(), request));
    }

    /**
     * Function: getMySlots (View schedule)
     * 
     * 1. TRIGGER: Provider Dashboard loads.
     * 
     * 2. LOGIC: Fetches all slots belonging to this logged-in provider.
     * 
     * 3. OUTCOME: Returns a list of slots to be displayed in the grid/table.
     */
    @GetMapping("/slots")
    public ResponseEntity<List<TimeSlotResponse>> getMySlots(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(timeSlotService.getProviderSlots(userDetails.getId()));
    }

    /**
     * Function: deleteSlot (Cancel availability)
     * 
     * 1. TRIGGER: Provider clicks "Trash"/"Delete" icon on a specific slot.
     * 
     * 2. LOGIC: 
     *    - Calls timeSlotService.cancelSlot().
     *    - If it was booked, it will auto-cancel the appointment and Notify the customer.
     * 
     * 3. OUTCOME: Slot is marked cancelled or deleted.
     */
    @DeleteMapping("/slots/{id}")
    public ResponseEntity<?> deleteSlot(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        timeSlotService.cancelSlot(userDetails.getId(), id);
        return ResponseEntity.ok().build();
    }

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
    
    @GetMapping("/slots/{id}/report")
    public ResponseEntity<org.springframework.core.io.InputStreamResource> exportAppointments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) throws java.io.IOException {
        
        // Fetch slot details to generate filename (You might need to add this method to Service if not exists, 
        // or just use a generic name for now if I can't easily fetch it. 
        // User asked for "Appointment_starttime_endtime".
        // I'll fetch valid slot first.)
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
