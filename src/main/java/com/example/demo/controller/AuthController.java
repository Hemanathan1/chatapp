package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        if (userRepo.findByUsername(user.getUsername()) != null) {
            return ResponseEntity.badRequest().body("Username already taken");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
        return ResponseEntity.ok("Registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        try {
            User found = userRepo.findByUsername(user.getUsername());
            if (found == null) {
                return ResponseEntity.status(401).body("User not found");
            }
            boolean match = passwordEncoder.matches(user.getPassword(), found.getPassword());
            if (!match) {
                return ResponseEntity.status(401).body("Wrong password");
            }
            return ResponseEntity.ok("Login successful. Welcome, " + found.getUsername());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

@Autowired
private com.example.demo.service.UserStatusService userStatusService;

@GetMapping("/online")
public java.util.Set<String> getOnlineUsers() {
    return userStatusService.getOnlineUsers();
}
}