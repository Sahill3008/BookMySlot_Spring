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

@RestController
@RequestMapping("/api/provider")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProviderController {

    private final TimeSlotService timeSlotService;

    public ProviderController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @PostMapping("/slots")
    public ResponseEntity<TimeSlotResponse> createSlot(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TimeSlotRequest request) {
        return ResponseEntity.ok(timeSlotService.createSlot(userDetails.getId(), request));
    }

    @GetMapping("/slots")
    public ResponseEntity<List<TimeSlotResponse>> getMySlots(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(timeSlotService.getProviderSlots(userDetails.getId()));
    }

    @DeleteMapping("/slots/{id}")
    public ResponseEntity<?> deleteSlot(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        timeSlotService.cancelSlot(userDetails.getId(), id);
        return ResponseEntity.ok().build();
    }
}
