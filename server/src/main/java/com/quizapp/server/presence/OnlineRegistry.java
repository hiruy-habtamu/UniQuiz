package com.quizapp.server.presence;

import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class OnlineRegistry {
    private final Map<Integer, Socket> onlineUsers = new ConcurrentHashMap<>();

    public void markOnline(int userId, Socket socket) {
        onlineUsers.put(userId, socket);
    }

    public void markOffline(int userId) {
        onlineUsers.remove(userId);
    }

    public boolean isOnline(int userId) {
        return onlineUsers.containsKey(userId);
    }

    public Optional<Socket> findSocket(int userId) {
        return Optional.ofNullable(onlineUsers.get(userId));
    }

    public int size() {
        return onlineUsers.size();
    }
}
