package com.bezkoder.spring.jpa.h2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

import com.bezkoder.spring.jpa.h2.model.Booking;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByUserId(int userId);
    List<Booking> findByUserIdAndDateOfBookingBefore(int userId, LocalDateTime now);
    List<Booking> findByUserIdAndDateOfBookingAfter(int userId, LocalDateTime now);
}
