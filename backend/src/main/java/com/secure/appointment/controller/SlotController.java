package com.secure.appointment.controller;

import com.secure.appointment.dto.response.TimeSlotResponse;
import com.secure.appointment.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * SlotController: Public Slot Viewer
 * 
 * What it does:
 * This controller allows ANYONE (even unauthenticated users) to see what slots are available.
 * It's key for the Home Page or "Browse Slots" page.
 */
@RestController
@RequestMapping("/api/slots")
public class SlotController {

    private final TimeSlotService timeSlotService;

    public SlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    /**
     * Function: getAvailableSlots
     * 
     * 1. TRIGGER: User lands on the Home Page ("Check Availability").
     * 
     * 2. LOGIC: Calls timeSlotService.getAvailableSlots() to find open spots.
     * 
     * 3. OUTCOME: Returns list of bookable slots.
     */
    @GetMapping
    public ResponseEntity<List<TimeSlotResponse>> getAvailableSlots() {
        return ResponseEntity.ok(timeSlotService.getAvailableSlots());
    }
}
