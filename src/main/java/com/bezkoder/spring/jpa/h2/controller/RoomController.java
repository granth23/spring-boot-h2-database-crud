package com.bezkoder.spring.jpa.h2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import com.bezkoder.spring.jpa.h2.model.Room;
import com.bezkoder.spring.jpa.h2.repository.RoomRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    // Get all rooms
    @GetMapping("/all")
    public ResponseEntity<List<Room>> getAllRooms() {
        List<Room> rooms = roomRepository.findAll();
        return ResponseEntity.ok(rooms);
    }

    // Create a new room
    @PostMapping("/add")
    public ResponseEntity<Object> addRoom(@RequestBody Room room) {
        if (room.getCapacity() < 0) {
            return ResponseEntity.badRequest().body(errorResponse("Invalid capacity"));
        }
        if (roomRepository.existsByRoomName(room.getRoomName())) {
            return new ResponseEntity<>(errorResponse("Room already exists"), HttpStatus.FORBIDDEN);
        }
        roomRepository.save(room);
        return ResponseEntity.ok("Room created successfully");
    }

    // Update a room
    @PatchMapping("/{roomId}")
    public ResponseEntity<Object> updateRoom(@PathVariable int roomId, @RequestBody Room roomDetails) {
        Optional<Room> roomData = roomRepository.findById(roomId);
        if (!roomData.isPresent()) {
            return new ResponseEntity<>(errorResponse("Room does not exist"), HttpStatus.NOT_FOUND);
        }
        Room room = roomData.get();
        if (roomDetails.getCapacity() < 0) {
            return ResponseEntity.badRequest().body(errorResponse("Invalid capacity"));
        }
        room.setRoomName(roomDetails.getRoomName());
        room.setCapacity(roomDetails.getCapacity());
        roomRepository.save(room);
        return ResponseEntity.ok("Room edited successfully");
    }

    // Delete a room
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Object> deleteRoom(@PathVariable int roomId) {
        if (!roomRepository.existsById(roomId)) {
            return new ResponseEntity<>(errorResponse("Room does not exist"), HttpStatus.NOT_FOUND);
        }
        roomRepository.deleteById(roomId);
        return ResponseEntity.ok("Room deleted successfully");
    }

    // Get a room by id
    @GetMapping("/{roomId}")
    public ResponseEntity<Object> getRoomById(@PathVariable int roomId) {
        Optional<Room> room = roomRepository.findById(roomId);
        if (!room.isPresent()) {
            return new ResponseEntity<>(errorResponse("Room does not exist"), HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(room.get());
    }

    private Map<String, String> errorResponse(String errorMessage) {
        Map<String, String> error = new HashMap<>();
        error.put("Error", errorMessage);
        return error;
    }
}
