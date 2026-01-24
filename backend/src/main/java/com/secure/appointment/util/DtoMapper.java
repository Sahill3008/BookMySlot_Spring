package com.secure.appointment.util;

import com.secure.appointment.dto.response.AppointmentResponse;
import com.secure.appointment.dto.response.TimeSlotResponse;
import com.secure.appointment.entity.Appointment;
import com.secure.appointment.entity.TimeSlot;

public class DtoMapper {

    public static TimeSlotResponse toTimeSlotResponse(TimeSlot slot) {
        return TimeSlotResponse.builder()
                .id(slot.getId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .isBooked(slot.isBooked())
                .capacity(slot.getCapacity())
                .bookedCount(slot.getBookedCount())
                .providerName(slot.getProvider().getName())
                .build();
    }

    public static AppointmentResponse toAppointmentResponse(Appointment appt) {
        return AppointmentResponse.builder()
                .id(appt.getId())
                .bookedAt(appt.getBookedAt())
                .status(appt.getStatus().name())
                .customerName(appt.getCustomer().getName())
                .slot(toTimeSlotResponse(appt.getSlot()))
                .build();
    }
}
