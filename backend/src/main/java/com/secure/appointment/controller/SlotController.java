package com.secure.appointment.controller;

import com.secure.appointment.dto.response.TimeSlotResponse;
import com.secure.appointment.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/slots")
public class SlotController {

    private final TimeSlotService timeSlotService;

    public SlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @GetMapping
    public ResponseEntity<List<TimeSlotResponse>> getAvailableSlots() {
        return ResponseEntity.ok(timeSlotService.getAvailableSlots());
    }
}
