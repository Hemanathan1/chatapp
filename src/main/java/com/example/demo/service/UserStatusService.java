package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserStatusService {

    private final Set<String> onlineUsers = Collections.synchronizedSet(new HashSet<>());

    public void setOnline(String username) {
        onlineUsers.add(username);
    }

    public void setOffline(String username) {
        onlineUsers.remove(username);
    }

    public boolean isOnline(String username) {
        return onlineUsers.contains(username);
    }

    public Set<String> getOnlineUsers() {
        return onlineUsers;
    }
}