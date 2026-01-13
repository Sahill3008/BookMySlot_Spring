package com.secure.appointment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class AppointmentRequest {
    @NotNull
    private Long slotId;

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }
}
