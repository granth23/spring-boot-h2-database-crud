package com.bezkoder.spring.jpa.h2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring.jpa.h2.model.Room;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    boolean existsByRoomName(String roomName);
}
