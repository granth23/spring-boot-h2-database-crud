package com.bezkoder.spring.jpa.h2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import com.bezkoder.spring.jpa.h2.model.Room;
import com.bezkoder.spring.jpa.h2.repository.RoomRepository;

import java.util.*;

@RestController
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;


    @GetMapping("/rooms")
    public ResponseEntity<List<Room>> getAllRooms() {
        List<Room> rooms = roomRepository.findAll();
        return ResponseEntity.ok(rooms);
    }


    @PostMapping("/rooms")
    public ResponseEntity<Object> addRoom(@RequestBody Room room) {
        if (roomRepository.existsByRoomName(room.getRoomName())) {
            return new ResponseEntity<>(errorResponse("Room already exists"), HttpStatus.FORBIDDEN);
        }
        if (room.getRoomCapacity() < 0) {
            return ResponseEntity.badRequest().body(errorResponse("Invalid capacity"));
        }
        roomRepository.save(room);
        return ResponseEntity.ok("Room created successfully");
    }


    @PatchMapping("/rooms")
    public ResponseEntity<Object> updateRoom(@RequestBody Room roomDetails) {
        int roomID = roomDetails.getRoomID();
        Optional<Room> roomData = roomRepository.findById(roomID);
        if (!roomData.isPresent()) {
            return new ResponseEntity<>(errorResponse("Room does not exist"), HttpStatus.NOT_FOUND);
        }
        Room room = roomData.get();
        if (roomDetails.getRoomCapacity() < 0) {
            return ResponseEntity.badRequest().body(errorResponse("Invalid room capacity"));
        }
        room.setRoomName(roomDetails.getRoomName());
        room.setRoomCapacity(roomDetails.getRoomCapacity());
        roomRepository.save(room);
        return ResponseEntity.ok("Room edited successfully");
    }


    @DeleteMapping("/rooms")
    public ResponseEntity<Object> deleteRoom(@RequestParam int roomID) {
        if (!roomRepository.existsById(roomID)) {
            return new ResponseEntity<>(errorResponse("Room does not exist"), HttpStatus.NOT_FOUND);
        }
        roomRepository.deleteById(roomID);
        return ResponseEntity.ok("Room deleted successfully");
    }


    @GetMapping("/rooms/{roomID}")
    public ResponseEntity<Object> getRoomById(@PathVariable int roomID) {
        Optional<Room> room = roomRepository.findById(roomID);
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
