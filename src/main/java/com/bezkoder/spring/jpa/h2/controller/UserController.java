package com.bezkoder.spring.jpa.h2.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bezkoder.spring.jpa.h2.model.User;
import com.bezkoder.spring.jpa.h2.repository.UserRepository;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class UserController {

  @Autowired
  private UserRepository userRepository;

  @PostMapping("/signup")
  public ResponseEntity<String> signup(@RequestBody User user) {
    if (userRepository.findByEmail(user.getEmail()) != null) {
      return new ResponseEntity<>("Forbidden, Account already exists", HttpStatus.FORBIDDEN);
    }
    userRepository.save(new User(user.getName(), user.getEmail(), user.getPassword()));
    return new ResponseEntity<>("Account Creation Successful", HttpStatus.CREATED);
  }

  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestBody User user) {
    User foundUser = userRepository.findByEmail(user.getEmail());
    if (foundUser == null) {
      return new ResponseEntity<>("User does not exist", HttpStatus.NOT_FOUND);
    }
    if (foundUser.getPassword().equals(user.getPassword())) {
      return new ResponseEntity<>("Login Successful", HttpStatus.OK);
    } else {
      return new ResponseEntity<>("Username/Password Incorrect", HttpStatus.UNAUTHORIZED);
    }
  }

  @GetMapping("/user")
  public ResponseEntity<?> getUserDetail(@RequestParam long userID) {
    Optional<User> user = userRepository.findById(userID);
    if (!user.isPresent()) {
      return new ResponseEntity<>("User does not exist", HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(user.get(), HttpStatus.OK);
  }

  @GetMapping("/users")
  public ResponseEntity<List<User>> getAllUsers() {
    List<User> users = userRepository.findAll();
    return new ResponseEntity<>(users, HttpStatus.OK);
  }

  @DeleteMapping("/users/{id}")
  public ResponseEntity<String> deleteUser(@PathVariable("id") long id) {
    Optional<User> user = userRepository.findById(id);
    if (!user.isPresent()) {
      return new ResponseEntity<>("User does not exist", HttpStatus.NOT_FOUND);
    }
    userRepository.deleteById(id);
    return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
  }
}
