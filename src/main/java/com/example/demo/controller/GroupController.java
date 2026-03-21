package com.example.demo.controller;

import com.example.demo.model.Group;
import com.example.demo.model.Message;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group")
public class GroupController {

    @Autowired
    private GroupRepository groupRepo;

    @Autowired
    private MessageRepository messageRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Create a new group
    @PostMapping("/create")
    public ResponseEntity<Group> createGroup(@RequestBody Group group) {
        // Make sure creator is in members list
        if (!group.getMembers().contains(group.getCreatedBy())) {
            group.getMembers().add(group.getCreatedBy());
        }
        Group saved = groupRepo.save(group);
        return ResponseEntity.ok(saved);
    }

    // Get all groups for a user
    @GetMapping("/my")
    public List<Group> getMyGroups(@RequestParam String username) {
        return groupRepo.findGroupsByMember(username);
    }

    // Get all messages for a group
    @GetMapping("/messages")
    public List<Message> getGroupMessages(@RequestParam Long groupId) {
        return messageRepo.findByGroupId(groupId);
    }

    // Send message to group
    @PostMapping("/send")
    public ResponseEntity<Message> sendGroupMessage(@RequestBody Message message) {
        Message saved = messageRepo.save(message);
        // Broadcast to all group members
        messagingTemplate.convertAndSend("/topic/group/" + message.getGroupId(), saved);
        return ResponseEntity.ok(saved);
    }
}