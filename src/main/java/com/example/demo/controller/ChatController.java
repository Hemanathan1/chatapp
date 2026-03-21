package com.example.demo.controller;

import com.example.demo.model.Message;
import com.example.demo.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageRepository messageRepo;

    // ── Send private message via WebSocket ──
    @MessageMapping("/chat")
    public void sendMessage(@Payload Message message) {
        message.setStatus("SENT");
        Message saved = messageRepo.save(message);
        saved.setStatus("DELIVERED");
        messagingTemplate.convertAndSend("/topic/messages/" + message.getReceiver(), saved);
        messagingTemplate.convertAndSend("/topic/messages/" + message.getSender(), saved);
    }

    // ── Mark messages as read ──
    @PostMapping("/message/read")
    public void markAsRead(@RequestParam String sender,
                           @RequestParam String receiver) {
        List<Message> messages = messageRepo.findConversation(sender, receiver);
        for (Message m : messages) {
            if (m.getReceiver() != null &&
                m.getReceiver().equals(receiver) &&
                !"READ".equals(m.getStatus())) {
                m.setStatus("READ");
                messageRepo.save(m);
            }
        }
        Message readReceipt = new Message();
        readReceipt.setSender(receiver);
        readReceipt.setReceiver(sender);
        readReceipt.setStatus("READ_RECEIPT");
        messagingTemplate.convertAndSend("/topic/messages/" + sender, readReceipt);
    }

    // ── Typing indicators ──
    @PostMapping("/message/typing")
    public void sendTyping(@RequestParam String sender,
                           @RequestParam String receiver) {
        Map<String, String> payload = new HashMap<>();
        payload.put("type", "TYPING");
        payload.put("sender", sender);
        messagingTemplate.convertAndSend("/topic/messages/" + receiver, payload);
    }

    @PostMapping("/message/stoptyping")
    public void stopTyping(@RequestParam String sender,
                           @RequestParam String receiver) {
        Map<String, String> payload = new HashMap<>();
        payload.put("type", "STOP_TYPING");
        payload.put("sender", sender);
        messagingTemplate.convertAndSend("/topic/messages/" + receiver, payload);
    }

    // ── Send file/image ──
    @PostMapping("/message/file")
    public ResponseEntity<Message> sendFile(
            @RequestParam("sender") String sender,
            @RequestParam("receiver") String receiver,
            @RequestParam("file") MultipartFile file) {
        try {
            String uploadDir = "uploads/chat/";
            Files.createDirectories(Paths.get(uploadDir));

            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }
            String filename = System.currentTimeMillis() + "_" + sender + ext;
            Path filePath = Paths.get(uploadDir + filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileType = file.getContentType() != null &&
                file.getContentType().startsWith("image") ? "image" : "file";

            Message msg = new Message();
            msg.setSender(sender);
            msg.setReceiver(receiver);
            msg.setContent(fileType.equals("image") ? "📷 Photo" : "📎 " + original);
            msg.setFileUrl("/uploads/chat/" + filename);
            msg.setFileName(original);
            msg.setFileType(fileType);
            msg.setStatus("SENT");
            Message saved = messageRepo.save(msg);

            saved.setStatus("DELIVERED");
            messagingTemplate.convertAndSend("/topic/messages/" + receiver, saved);
            messagingTemplate.convertAndSend("/topic/messages/" + sender, saved);

            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    // ── Get all messages ──
    @GetMapping("/message/all")
    public List<Message> getAllMessages() {
        return messageRepo.findAll();
    }

    // ── Get conversation ──
    @GetMapping("/message/conversation")
    public List<Message> getConversation(
            @RequestParam String user1,
            @RequestParam String user2) {
        return messageRepo.findConversation(user1, user2);
    }

    // ── Search messages ──
    @GetMapping("/message/search")
    public List<Message> searchMessages(
            @RequestParam String user1,
            @RequestParam String user2,
            @RequestParam String query) {
        return messageRepo.searchConversation(user1, user2, query);
    }

    // ── Delete message ──
    @DeleteMapping("/message/delete/{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long id) {
        if (!messageRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        messageRepo.deleteById(id);
        return ResponseEntity.ok("Deleted");
    }

    // ── Edit message ──
    @PutMapping("/message/edit/{id}")
    public ResponseEntity<Message> editMessage(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return messageRepo.findById(id).map(msg -> {
            msg.setContent(body.get("content") + " (edited)");
            return ResponseEntity.ok(messageRepo.save(msg));
        }).orElse(ResponseEntity.notFound().build());
    }
}