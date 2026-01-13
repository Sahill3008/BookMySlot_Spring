package com.secure.appointment.repository;

import com.secure.appointment.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    // Find slots by provider (active only)
    List<TimeSlot> findByProviderIdAndIsCancelledFalseOrderByStartTimeAsc(Long providerId);

    // Find overlapping slots for validation
    // Find overlapping slots for validation
    // start < existingEnd AND end > existingStart AND isCancelled = false
    boolean existsByProviderIdAndStartTimeLessThanAndEndTimeGreaterThanAndIsCancelledFalse(
            Long providerId, LocalDateTime endTime, LocalDateTime startTime);

    // Find available slots for customers (future only, not cancelled)
    List<TimeSlot> findByIsBookedFalseAndIsCancelledFalseAndStartTimeAfterOrderByStartTimeAsc(LocalDateTime now);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT ts FROM TimeSlot ts WHERE ts.id = :id")
    java.util.Optional<TimeSlot> findByIdWithLock(@org.springframework.data.repository.query.Param("id") Long id);
}
