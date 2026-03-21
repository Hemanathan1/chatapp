package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepo;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadPic(
            @RequestParam("username") String username,
            @RequestParam("file") MultipartFile file) {

        try {
            // Save to uploads/ folder in project root
            String uploadDir = "uploads/";
            Files.createDirectories(Paths.get(uploadDir));

            // Clean filename
            String ext = "";
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }
            String filename = username + ext;
            Path filePath = Paths.get(uploadDir + filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Save URL to DB
            String picUrl = "/uploads/" + filename;
            User user = userRepo.findByUsername(username);
            if (user == null) return ResponseEntity.notFound().build();
            user.setProfilePic(picUrl);
            userRepo.save(user);

            return ResponseEntity.ok(picUrl);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/pic")
    public ResponseEntity<String> getPic(@RequestParam String username) {
        User user = userRepo.findByUsername(username);
        if (user == null || user.getProfilePic() == null) {
            return ResponseEntity.ok("");
        }
        return ResponseEntity.ok(user.getProfilePic());
    }
}