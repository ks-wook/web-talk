package com.example.chat.service;

import com.example.chat.dto.RoomDto;
import com.example.chat.model.request.CreateRoomRequest;
import com.example.chat.model.response.ChatListResponse;
import com.example.chat.model.response.CreateRoomResponse;
import com.example.chat.model.response.JoinedRoomListResponse;

import java.util.List;

public interface ChatService {

    /**
     * 새로운 채팅 방 생성
     *
     * @param authString 인증 토큰
     * @param request    채팅방 생성 요청
     * @return 생성된 채팅방 정보
     */
    CreateRoomResponse createRoom(String authString, CreateRoomRequest request);

    /**
     * UserId로 참여중인 채팅방 목록 조회
     *
     * @param authString 인증 토큰
     * @return 참여중인 채팅방 목록
     */
    JoinedRoomListResponse getJoinedRoomsByUserId(String authString);

    /**
     * UserId 기준 채팅방 조회
     *
     * @param userId 사용자 ID
     * @return 채팅방 목록
     */
    List<RoomDto> findRoomsByUserId(Long userId);

    /**
     * 채팅방의 최근 메시지 100개 조회
     *
     * @param roomId 채팅방 ID
     * @return 최근 채팅 메시지 목록
     */
    ChatListResponse getRecentMessagesInRoom(Long roomId);
}
