package com.bezkoder.spring.jpa.h2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import com.bezkoder.spring.jpa.h2.model.Booking;
import com.bezkoder.spring.jpa.h2.model.Room;
import com.bezkoder.spring.jpa.h2.repository.BookingRepository;
import com.bezkoder.spring.jpa.h2.repository.RoomRepository;
import com.bezkoder.spring.jpa.h2.repository.UserRepository;
import com.bezkoder.spring.jpa.h2.model.User;
import com.bezkoder.spring.jpa.h2.model.BookingRequest;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/book")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoomRepository roomRepository;

    // Create a new Booking
    @PostMapping
    public ResponseEntity<String> createBooking(@RequestBody BookingRequest bookingRequest) {
        Optional<User> user = userRepository.findById((long) bookingRequest.getUserId());
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist");
        }

        Optional<Room> room = roomRepository.findById(bookingRequest.getRoomId());
        if (!room.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room does not exist");
        }

        Booking booking = new Booking();
        booking.setUser(user.get());
        booking.setRoom(room.get());
        booking.setDateOfBooking(
                LocalDateTime.parse(bookingRequest.getDateOfBooking() + "T" + bookingRequest.getTimeFrom()));
        booking.setTimeFrom(bookingRequest.getTimeFrom());
        booking.setTimeTo(bookingRequest.getTimeTo());
        booking.setPurpose(bookingRequest.getPurpose());

        // Here you would check availability and other business logic like time
        // validation
        bookingRepository.save(booking);
        return ResponseEntity.ok("Booking created successfully");
    }

    // Update an existing booking
    @PatchMapping("/{bookingId}")
    public ResponseEntity<String> updateBooking(@PathVariable int bookingId, @RequestBody Booking bookingDetails) {
        Optional<Booking> existingBooking = bookingRepository.findById(bookingId);
        if (!existingBooking.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Booking booking = existingBooking.get();
        // Here you would check availability and other business logic like time
        // validation
        booking.setDateOfBooking(bookingDetails.getDateOfBooking());
        booking.setTimeFrom(bookingDetails.getTimeFrom());
        booking.setTimeTo(bookingDetails.getTimeTo());
        booking.setPurpose(bookingDetails.getPurpose());
        bookingRepository.save(booking);
        return ResponseEntity.ok("Booking modified successfully");
    }

    // Delete a booking
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<String> deleteBooking(@PathVariable int bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking does not exist");
        }
        bookingRepository.deleteById(bookingId);
        return ResponseEntity.ok("Booking deleted successfully");
    }
}
