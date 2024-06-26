package com.bezkoder.spring.jpa.h2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bezkoder.spring.jpa.h2.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
  User findByEmail(String email);
}
