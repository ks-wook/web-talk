package com.example.chat.controller;

import com.example.chat.model.request.CreateRoomRequest;
import com.example.chat.model.response.ChatListResponse;
import com.example.chat.model.response.CreateRoomResponse;
import com.example.chat.model.response.JoinedRoomListResponse;
import com.example.chat.service.ChatService;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Chat API", description = "V1 Chat API")
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatControllerV1 {

    private final ChatService chatService;

    @Operation(
            summary = "참여 중인 방 목록을 가져옵니다.",
            description = "유저가 참여 중인 채팅 방 목록을 가져옵니다."
    )
    @GetMapping("/get-joined-rooms")
    public JoinedRoomListResponse getJoinedRooms(
            @RequestHeader("Authorization") String authString
    ) {
        return chatService.getJoinedRoomsByUserId(authString);
    }

    @Operation(
            summary = "특정 방의 최근 100개의 채팅 내역을 가져옵니다.",
            description = "방 ID와 제한된 개수의 채팅 기록을 가져옵니다."
    )
    @GetMapping("/rooms/{roomId}/messages")
    public ChatListResponse roomMessages(
            @PathVariable("roomId") Long roomId,
            @RequestHeader("Authorization") String authString
    ) {
        return chatService.getRecentMessagesInRoom(roomId);
    }

    @Operation(
            summary = "새로운 채팅 방을 생성합니다.",
            description = "새로운 채팅 방을 생성하고 해당 방의 정보를 반환합니다."
    )
    @PostMapping("/create-room")
    public CreateRoomResponse createRoom(
            @RequestHeader("Authorization") String authString,
            @RequestBody CreateRoomRequest request
    ) {
        return chatService.createRoom(authString, request);
    }
}
