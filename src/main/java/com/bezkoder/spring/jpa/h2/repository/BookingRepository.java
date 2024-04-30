package com.bezkoder.spring.jpa.h2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import com.bezkoder.spring.jpa.h2.model.Booking;
import com.bezkoder.spring.jpa.h2.model.Room;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByRoomAndDateOfBookingAndTimeToGreaterThanEqualAndTimeFromLessThanEqual(
        Room room, LocalDateTime date, String timeFrom, String timeTo);
    List<Booking> findByUserUserID(long userID);
}
