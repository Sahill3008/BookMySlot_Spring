package com.secure.appointment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class TimeSlotResponse {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isBooked;
    private String providerName;

    public TimeSlotResponse() {
    }

    public TimeSlotResponse(Long id, LocalDateTime startTime, LocalDateTime endTime, boolean isBooked, String providerName) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isBooked = isBooked;
        this.providerName = providerName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private boolean isBooked;
        private String providerName;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder isBooked(boolean isBooked) {
            this.isBooked = isBooked;
            return this;
        }

        public Builder providerName(String providerName) {
            this.providerName = providerName;
            return this;
        }

        public TimeSlotResponse build() {
            return new TimeSlotResponse(id, startTime, endTime, isBooked, providerName);
        }
    }
}
