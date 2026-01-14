package com.example.chat.service;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

@Component
public class InMemoryRoomManager implements RoomManager {

    private static final Logger logger =
            LoggerFactory.getLogger(InMemoryRoomManager.class);

    /**
     * roomId -> WebSocketSession Set
     */
    private final ConcurrentMap<Long, Set<WebSocketSession>> roomSessions =
            new ConcurrentHashMap<>();

    /**
     * session -> roomId Set
     * (세션 종료 시 빠른 정리를 위해 역참조 유지)
     */
    private final ConcurrentMap<WebSocketSession, Set<Long>> sessionRooms =
            new ConcurrentHashMap<>();

    /**
     * 세션을 특정 방에 추가
     * @param roomId
     * @param userId
     * @param session
     */
    @Override
    public void joinRoom(Long roomId, Long userId, WebSocketSession session) {
        roomSessions
                .computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet())
                .add(session);

        sessionRooms
                .computeIfAbsent(session, k -> ConcurrentHashMap.newKeySet())
                .add(roomId);

        logger.info("User {} joined room {}", userId, roomId);
    }

    /**
     * 세션을 여러 방에 추가
     * @param roomIds
     * @param userId
     * @param session
     */
    @Override
    public void joinRooms(Set<Long> roomIds, Long userId, WebSocketSession session) {
        for (Long roomId : roomIds) {
            joinRoom(roomId, userId, session);
        }
    }

    /**
     * 세션을 특정 방에서 제거
     * @param roomId
     * @param session
     */
    @Override
    public void leaveRoom(Long roomId, WebSocketSession session) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session);

            if (sessions.isEmpty()) {
                roomSessions.remove(roomId);
            }
        }

        Set<Long> rooms = sessionRooms.get(session);
        if (rooms != null) {
            rooms.remove(roomId);

            if (rooms.isEmpty()) {
                sessionRooms.remove(session);
            }
        }

        logger.info("Session left room {}", roomId);
    }

    /**
     * 세션을 모든 방에서 제거
     * @param session
     */
    @Override
    public void removeSession(WebSocketSession session, Consumer<Long> onRoomEmpty) {
        Set<Long> rooms = sessionRooms.remove(session);
        if (rooms == null) {
            return;
        }

        for (Long roomId : rooms) {
            Set<WebSocketSession> sessions = roomSessions.get(roomId);
            if (sessions != null) {
                sessions.remove(session);

                // 방이 비어있는 경우 방 정보도 제거
                if (sessions.isEmpty()) {
                    roomSessions.remove(roomId);
                    onRoomEmpty.accept(roomId);
                }
            }
        }

        logger.info("Session removed from all rooms");
    }

    @Override
    public Set<WebSocketSession> getSessions(Long roomId) {
        return roomSessions.getOrDefault(roomId, Collections.emptySet());
    }

    @Override
    public ConcurrentMap<Long, Set<WebSocketSession>> getAllRoomSessions() {
        return roomSessions;
    }
}
