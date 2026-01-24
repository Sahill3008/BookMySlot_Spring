package com.secure.appointment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "time_slots", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "provider_id", "start_time" })
}, indexes = {
        @Index(name = "idx_timeslot_starttime", columnList = "start_time")
})
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "is_booked", nullable = false)
    private boolean isBooked = false;

    @Column(name = "capacity", nullable = false)
    private int capacity = 1;

    @Column(name = "booked_count", nullable = false)
    private int bookedCount = 0;

    @Column(name = "is_cancelled")
    private Boolean isCancelled = false;


    @Version
    private Long version; // Optimistic Locking

    public TimeSlot() {
    }

    public TimeSlot(Long id, User provider, LocalDateTime startTime, LocalDateTime endTime, boolean isBooked, int capacity, int bookedCount, Long version) {
        this.id = id;
        this.provider = provider;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isBooked = isBooked;
        this.capacity = capacity;
        this.bookedCount = bookedCount;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getProvider() {
        return provider;
    }

    public void setProvider(User provider) {
        this.provider = provider;
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

    public boolean isCancelled() {
        return Boolean.TRUE.equals(isCancelled);
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getBookedCount() {
        return bookedCount;
    }

    public void setBookedCount(int bookedCount) {
        this.bookedCount = bookedCount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private User provider;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private boolean isBooked = false;
        private int capacity = 1;
        private int bookedCount = 0;
        private Boolean isCancelled = false;
        private Long version;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder provider(User provider) {
            this.provider = provider;
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

        public Builder capacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public Builder bookedCount(int bookedCount) {
            this.bookedCount = bookedCount;
            return this;
        }

        public Builder isCancelled(boolean isCancelled) {
            this.isCancelled = isCancelled;
            return this;
        }

        public Builder version(Long version) {
            this.version = version;
            return this;
        }

        public TimeSlot build() {
            TimeSlot slot = new TimeSlot(id, provider, startTime, endTime, isBooked, capacity, bookedCount, version);
            slot.setCancelled(isCancelled);
            return slot;
        }
    }
}
