package com.secure.appointment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class AppointmentResponse {
    private Long id;
    private LocalDateTime bookedAt;
    private String status;
    private TimeSlotResponse slot;
    private String customerName;

    public AppointmentResponse() {
    }

    public AppointmentResponse(Long id, LocalDateTime bookedAt, String status, TimeSlotResponse slot, String customerName) {
        this.id = id;
        this.bookedAt = bookedAt;
        this.status = status;
        this.slot = slot;
        this.customerName = customerName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(LocalDateTime bookedAt) {
        this.bookedAt = bookedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public TimeSlotResponse getSlot() {
        return slot;
    }

    public void setSlot(TimeSlotResponse slot) {
        this.slot = slot;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private LocalDateTime bookedAt;
        private String status;
        private TimeSlotResponse slot;
        private String customerName;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder bookedAt(LocalDateTime bookedAt) {
            this.bookedAt = bookedAt;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder slot(TimeSlotResponse slot) {
            this.slot = slot;
            return this;
        }

        public Builder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public AppointmentResponse build() {
            return new AppointmentResponse(id, bookedAt, status, slot, customerName);
        }
    }
}
