package com.example.parkease.repository;

import com.example.parkease.entity.Booking;
import com.example.parkease.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository
        extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Booking> findByStatus(BookingStatus status);

    @Query("""
        SELECT COUNT(b) > 0
        FROM Booking b
        WHERE b.slot.id = :slotId
          AND b.status IN :statuses
          AND b.startTime < :endTime
          AND b.endTime > :startTime
        """)
    boolean existsOverlappingBooking(
            @Param("slotId") Long slotId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("statuses") List<BookingStatus> statuses
    );

    List<Booking> findBySlotParkingLotOwnerIdOrderByCreatedAtDesc(
            Long ownerId
    );
}