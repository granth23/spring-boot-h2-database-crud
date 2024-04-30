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
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoomRepository roomRepository;

    // Create a new Booking
    @PostMapping("/book")
    public ResponseEntity<Object> createBooking(@RequestBody BookingRequest bookingRequest) {
        System.out.println("Booking: " + bookingRequest.getUserId() + " " + bookingRequest.getRoomId() + " "
                + bookingRequest.getDateOfBooking() + " " + bookingRequest.getTimeFrom() + " " + bookingRequest.getTimeTo()
                + " " + bookingRequest.getPurpose());
        Optional<User> user = userRepository.findById((long) bookingRequest.getUserId());
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse("User does not exist"));
        }

        Optional<Room> room = roomRepository.findById(bookingRequest.getRoomId());
        if (!room.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse("Room does not exist"));
        }

        Booking booking = new Booking();
        booking.setUser(user.get());
        booking.setRoom(room.get());
        booking.setDateOfBooking(
                LocalDateTime.parse(bookingRequest.getDateOfBooking() + "T" + bookingRequest.getTimeFrom()));
        booking.setTimeFrom(bookingRequest.getTimeFrom());
        booking.setTimeTo(bookingRequest.getTimeTo());
        booking.setPurpose(bookingRequest.getPurpose());

        // Here you would check availability and other business logic like time validation
        bookingRepository.save(booking);
        return ResponseEntity.ok("Booking created successfully");
    }

    // Update an existing booking
    @PatchMapping("/book")
    public ResponseEntity<Object> updateBooking(@RequestParam int bookingId, @RequestBody BookingRequest bookingRequest) {
        Optional<Booking> existingBooking = bookingRepository.findById(bookingId);
        if (!existingBooking.isPresent()) {
            return new ResponseEntity<>(errorResponse("Booking does not exist"), HttpStatus.NOT_FOUND);
        }
        Booking booking = existingBooking.get();

        // Assuming date and time updates are allowed
        LocalDateTime newBookingDateTime = LocalDateTime.parse(bookingRequest.getDateOfBooking() + "T" + bookingRequest.getTimeFrom());
        booking.setDateOfBooking(newBookingDateTime);
        booking.setTimeFrom(bookingRequest.getTimeFrom());
        booking.setTimeTo(bookingRequest.getTimeTo());
        booking.setPurpose(bookingRequest.getPurpose());

        // Save the updated booking
        bookingRepository.save(booking);

        return ResponseEntity.ok("Booking modified successfully");
    }


    // Delete a booking
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Object> deleteBooking(@PathVariable int bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse("Booking does not exist"));
        }
        bookingRepository.deleteById(bookingId);
        return ResponseEntity.ok("Booking deleted successfully");
    }

    // Endpoint to retrieve booking history
    @GetMapping("/history")
    public ResponseEntity<?> getBookingHistory(@RequestParam int userID) {
        Optional<User> user = userRepository.findById((long) userID);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse("User does not exist"));
        }

        // Fetch past bookings using the user ID and a date filter
        List<Booking> bookings = bookingRepository.findByUserIdAndDateOfBookingBefore(userID, LocalDateTime.now());

        List<Map<String, Object>> result = transformBookingsToResponse(bookings);
        return ResponseEntity.ok(result);
    }

    // Endpoint to retrieve upcoming room bookings
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingBookings(@RequestParam int userID) {
        Optional<User> user = userRepository.findById((long) userID);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse("User does not exist"));
        }

        List<Booking> bookings = bookingRepository.findByUserIdAndDateOfBookingAfter(userID, LocalDateTime.now());

        List<Map<String, Object>> result = transformBookingsToResponse(bookings);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<Map<String, Object>>> getAllBookings() {
        // Fetch all bookings from the repository
        List<Booking> bookings = bookingRepository.findAll();

        List<Map<String, Object>> result = transformBookingsToResponse(bookings);
        return ResponseEntity.ok(result);
    }

    private Map<String, String> errorResponse(String errorMessage) {
        Map<String, String> error = new HashMap<>();
        error.put("Error", errorMessage);
        return error;
    }

    private List<Map<String, Object>> transformBookingsToResponse(List<Booking> bookings) {
        return bookings.stream().map(booking -> {
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
    }
}
