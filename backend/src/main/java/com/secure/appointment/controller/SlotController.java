package com.secure.appointment.controller;

import com.secure.appointment.dto.response.TimeSlotResponse;
import com.secure.appointment.service.TimeSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/slots")
@Tag(name = "Public Slots", description = "Browsing Availability for Everyone")
public class SlotController {

    private final TimeSlotService timeSlotService;

    public SlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @Operation(
        summary = "Browse Available Slots",
        description = """
            ### 1. HUMAN SUMMARY
            This endpoint shows a list of *future* time slots that are currently empty and ready to be booked.
            It is public, meaning anyone visiting the site can see these times without logging in.
            
            ### 2. REAL-WORLD SCENARIO
            A guest visits the "Home" page. They scroll through the calendar to see if there are any openings next Tuesday.
            This API provides that list, page by page.
            
            ### 3. REQUEST EXPLANATION
            - **Source**: Query Parameters (URL)
            - **Optional Parameters**:
                - `page`: Page number (0 = first page). Default is 0.
                - `size`: How many items per page. Default is usually 10 or 20.
                - `sort`: Sorting criteria (e.g., `startTime,asc`).
            
            ### 4. AUTH SECTION
            - **No Authentication Required**: Public access.
            
            ### 5. RESPONSE GUIDE
            - **200 OK**: Returns a Paged result.
                - `content`: The list of slots.
                - `totalPages`: Total number of pages available.
                - `totalElements`: Total number of slots available.
            
            ### 6. ERROR DIAGNOSIS
            - **Empty Content**: "No slots available". This means fully booked or no slots created yet.
            """
    )
    @Parameter(name = "page", description = "Page number (0..N)", example = "0")
    @Parameter(name = "size", description = "Items per page", example = "10")
    @Parameter(name = "sort", description = "Sort criteria", example = "startTime,asc")
    @ApiResponse(responseCode = "200", description = "Slots retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<TimeSlotResponse>> getAvailableSlots(
            Pageable pageable) {
        return ResponseEntity.ok(timeSlotService.getAvailableSlots(pageable));
    }
}
