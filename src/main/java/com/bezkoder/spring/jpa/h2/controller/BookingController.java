package com.bezkoder.spring.jpa.h2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.*;

import com.bezkoder.spring.jpa.h2.model.Booking;
import com.bezkoder.spring.jpa.h2.model.Room;
import com.bezkoder.spring.jpa.h2.repository.BookingRepository;
import com.bezkoder.spring.jpa.h2.repository.RoomRepository;
import com.bezkoder.spring.jpa.h2.repository.UserRepository;
import com.bezkoder.spring.jpa.h2.model.User;
import com.bezkoder.spring.jpa.h2.model.BookingRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;


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

    @GetMapping("/history")
    public ResponseEntity<?> getBookingHistory(@RequestParam int userId) {
        Optional<User> user = userRepository.findById((long) userId);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist");
        }

        // Fetch past bookings using the user ID and a date filter
        List<Booking> bookings = bookingRepository.findByUserIdAndDateOfBookingBefore(userId, LocalDateTime.now());

        // Convert each booking into a Map structure for the response
        List<Map<String, Object>> result = bookings.stream().map(booking -> {
            Map<String, Object> bookingMap = new HashMap<>();
            bookingMap.put("room", booking.getRoom().getRoomName());
            bookingMap.put("roomID", booking.getRoom().getRoomId());
            bookingMap.put("bookingID", booking.getBookingId());
            bookingMap.put("dateOfBooking", booking.getDateOfBooking().toString());
            bookingMap.put("timeFrom", booking.getTimeFrom());
            bookingMap.put("timeTo", booking.getTimeTo());
            bookingMap.put("purpose", booking.getPurpose());
            return bookingMap;
        }).collect(Collectors.toList());

        // Return the list of booking details
        return ResponseEntity.ok(result);
    }

    // Endpoint to retrieve upcoming room bookings
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingBookings(@RequestParam int userId) {
        Optional<User> user = userRepository.findById((long) userId);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist");
        }

        List<Booking> bookings = bookingRepository.findByUserIdAndDateOfBookingAfter(userId, LocalDateTime.now());

        List<Map<String, Object>> result = bookings.stream().map(booking -> {
            Map<String, Object> bookingMap = new HashMap<>();
            bookingMap.put("room", booking.getRoom().getRoomName());
            bookingMap.put("roomID", booking.getRoom().getRoomId());
            bookingMap.put("bookingID", booking.getBookingId());
            bookingMap.put("dateOfBooking", booking.getDateOfBooking().toString());
            bookingMap.put("timeFrom", booking.getTimeFrom());
            bookingMap.put("timeTo", booking.getTimeTo());
            bookingMap.put("purpose", booking.getPurpose());
            return bookingMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

     @GetMapping("/bookings")
    public ResponseEntity<List<Map<String, Object>>> getAllBookings() {
        // Fetch all bookings from the repository
        List<Booking> bookings = bookingRepository.findAll();

        // Convert each booking to a Map structure for the response
        List<Map<String, Object>> result = bookings.stream().map(booking -> {
            Map<String, Object> bookingMap = new HashMap<>();
            bookingMap.put("room", booking.getRoom().getRoomName());
            bookingMap.put("roomID", booking.getRoom().getRoomId());
            bookingMap.put("bookingID", booking.getBookingId());
            bookingMap.put("dateOfBooking", booking.getDateOfBooking().toString());
            bookingMap.put("timeFrom", booking.getTimeFrom());
            bookingMap.put("timeTo", booking.getTimeTo());
            bookingMap.put("purpose", booking.getPurpose());
            return bookingMap;
        }).collect(Collectors.toList());

        // Return the list of booking details
        return ResponseEntity.ok(result);
    }

}
