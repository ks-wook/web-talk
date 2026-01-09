package com.example.chat.service;

import org.springframework.web.socket.WebSocketSession;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public interface RoomManager {

    void joinRoom(Long roomId, Long userId, WebSocketSession session);

    void joinRooms(Set<Long> roomIds, Long userId, WebSocketSession session);

    void leaveRoom(Long roomId, WebSocketSession session);

    void removeSession(WebSocketSession session, Consumer<Long> onRoomEmpty);

    Set<WebSocketSession> getSessions(Long roomId);

    ConcurrentMap<Long, Set<WebSocketSession>> getAllRoomSessions();
}
