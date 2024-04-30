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
        Optional<User> user = userRepository.findById((long) bookingRequest.getUserID());
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse("User does not exist"));
        }

        Optional<Room> room = roomRepository.findById(bookingRequest.getRoomID());
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

        if (booking.getTimeFrom().compareTo(booking.getTimeTo()) >= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse("Invalid time range"));
        }

        List<Booking> conflictingBookings = bookingRepository
                .findByRoomAndDateOfBookingAndTimeToGreaterThanEqualAndTimeFromLessThanEqual(
                        room.get(), booking.getDateOfBooking(), booking.getTimeFrom(), booking.getTimeTo());

        if (!conflictingBookings.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(errorResponse("Room is already booked at the specified time"));
        }
        bookingRepository.save(booking);
        return ResponseEntity.ok("Booking created successfully");
    }

    // Update an existing booking
    @PatchMapping("/book")
    public ResponseEntity<Object> updateBooking(@RequestBody BookingRequest bookingRequest) {
        Optional<Booking> existingBooking = bookingRepository.findById(bookingRequest.getBookingID());
        if (!existingBooking.isPresent()) {
            return new ResponseEntity<>(errorResponse("Booking does not exist"), HttpStatus.NOT_FOUND);
        }
        Booking booking = existingBooking.get();

        // Assuming date and time updates are allowed
        LocalDateTime newBookingDateTime = LocalDateTime
                .parse(bookingRequest.getDateOfBooking() + "T" + bookingRequest.getTimeFrom());
        booking.setDateOfBooking(newBookingDateTime);
        booking.setTimeFrom(bookingRequest.getTimeFrom());
        booking.setTimeTo(bookingRequest.getTimeTo());
        booking.setPurpose(bookingRequest.getPurpose());

        // Check for conflicting bookings
        List<Booking> conflictingBookings = bookingRepository
                .findByRoomAndDateOfBookingAndTimeToGreaterThanEqualAndTimeFromLessThanEqual(
                        booking.getRoom(), booking.getDateOfBooking(), booking.getTimeFrom(), booking.getTimeTo());
        if (!conflictingBookings.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(errorResponse("Room is already booked at the specified time"));
        }

        // Save the updated booking
        bookingRepository.save(booking);
        return ResponseEntity.ok("Booking modified successfully");
    }

    // Delete a booking
    @DeleteMapping("/book")
    public ResponseEntity<Object> deleteBooking(@RequestParam int bookingID) {
        if (!bookingRepository.existsById(bookingID)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse("Booking does not exist"));
        }
        bookingRepository.deleteById(bookingID);
        return ResponseEntity.ok("Booking deleted successfully");
    }

    // Endpoint to retrieve booking history
    @GetMapping("/history")
    public ResponseEntity<?> getBookingHistory(@RequestParam int userID) {
        Optional<User> user = userRepository.findById((long) userID);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse("User does not exist"));
        }
        List<Booking> bookings = bookingRepository.findByUserUserID(userID);
        List<Booking> pastBookings = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.getDateOfBooking().isBefore(LocalDateTime.now())) {
                pastBookings.add(booking);
            } else {
                continue;
            }
        }
        List<Map<String, Object>> result = transformBookingsToResponse(pastBookings);
        for (Map<String, Object> bookingMap : result) {
            String dateOfBooking = (String) bookingMap.get("dateOfBooking");
            if (dateOfBooking.contains("T")) {
                dateOfBooking = dateOfBooking.substring(0, dateOfBooking.indexOf("T"));
                bookingMap.put("dateOfBooking", dateOfBooking);
            }
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingBookings(@RequestParam int userID) {
        Optional<User> user = userRepository.findById((long) userID);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse("User does not exist"));
        }
        List<Booking> bookings = bookingRepository.findByUserUserID(userID);
        List<Booking> upcomingBookings = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.getDateOfBooking().isAfter(LocalDateTime.now())) {
                upcomingBookings.add(booking);
            } else if (booking.getTimeTo().compareTo(LocalDateTime.now().toString()) > 0) {
                upcomingBookings.add(booking);
            } else {
                continue;
            }
        }
        List<Map<String, Object>> result = transformBookingsToResponse(upcomingBookings);
        for (Map<String, Object> bookingMap : result) {
            String dateOfBooking = (String) bookingMap.get("dateOfBooking");
            if (dateOfBooking.contains("T")) {
                dateOfBooking = dateOfBooking.substring(0, dateOfBooking.indexOf("T"));
                bookingMap.put("dateOfBooking", dateOfBooking);
            }
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<Map<String, Object>>> getAllBookings() {
        // Fetch all bookings from the repository
        List<Booking> bookings = bookingRepository.findAll();

        List<Map<String, Object>> result = transformBookingsToResponse(bookings);
        for (Map<String, Object> bookingMap : result) {
            String dateOfBooking = (String) bookingMap.get("dateOfBooking");
            if (dateOfBooking.contains("T")) {
                dateOfBooking = dateOfBooking.substring(0, dateOfBooking.indexOf("T"));
                bookingMap.put("dateOfBooking", dateOfBooking);
            }
        }
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
            bookingMap.put("roomID", booking.getRoom().getRoomID());
            bookingMap.put("bookingID", booking.getBookingId());
            bookingMap.put("dateOfBooking", booking.getDateOfBooking().toString());
            bookingMap.put("timeFrom", booking.getTimeFrom());
            bookingMap.put("timeTo", booking.getTimeTo());
            bookingMap.put("purpose", booking.getPurpose());
            return bookingMap;
        }).collect(Collectors.toList());
    }
}
