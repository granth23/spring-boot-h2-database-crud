package com.bezkoder.spring.jpa.h2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import com.bezkoder.spring.jpa.h2.model.Room;
import com.bezkoder.spring.jpa.h2.repository.RoomRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    // Get all rooms or filter by date, time, capacity
    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        List<Room> rooms = roomRepository.findAll();
        return ResponseEntity.ok(rooms);
    }

    // Create a new room
    @PostMapping
    public ResponseEntity<String> addRoom(@RequestBody Room room) {
        if (room.getCapacity() < 0) {
            return ResponseEntity.badRequest().body("Invalid capacity");
        }
        if (roomRepository.existsByRoomName(room.getRoomName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Room already exists");
        }
        roomRepository.save(room);
        return ResponseEntity.ok("Room created successfully");
    }

    // Update a room
    @PatchMapping("/{roomId}")
    public ResponseEntity<String> updateRoom(@PathVariable int roomId, @RequestBody Room roomDetails) {
        Optional<Room> roomData = roomRepository.findById(roomId);
        if (!roomData.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Room room = roomData.get();
        if (roomDetails.getCapacity() < 0) {
            return ResponseEntity.badRequest().body("Invalid capacity");
        }
        room.setRoomName(roomDetails.getRoomName());
        room.setCapacity(roomDetails.getCapacity());
        roomRepository.save(room);
        return ResponseEntity.ok("Room edited successfully");
    }

    // Delete a room
    @DeleteMapping("/{roomId}")
    public ResponseEntity<String> deleteRoom(@PathVariable int roomId) {
        if (!roomRepository.existsById(roomId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room does not exist");
        }
        roomRepository.deleteById(roomId);
        return ResponseEntity.ok("Room deleted successfully");
    }

    // Get a room by id
    @GetMapping("/{roomId}")
    public ResponseEntity<?> getRoomById(@PathVariable int roomId) {
        Optional<Room> room = roomRepository.findById(roomId);
        if (!room.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room does not exist");
        }
        return ResponseEntity.ok(room.get());
    }
}
