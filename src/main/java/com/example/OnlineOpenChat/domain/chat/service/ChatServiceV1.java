package com.example.OnlineOpenChat.domain.chat.service;

import com.example.OnlineOpenChat.common.Constants.RedisMessageType;
import com.example.OnlineOpenChat.common.exception.CustomException;
import com.example.OnlineOpenChat.common.exception.ErrorCode;
import com.example.OnlineOpenChat.domain.chat.model.Room;
import com.example.OnlineOpenChat.domain.chat.model.RoomDto;
import com.example.OnlineOpenChat.domain.chat.model.RoomMember;
import com.example.OnlineOpenChat.domain.chat.model.request.CreateRoomRequest;
import com.example.OnlineOpenChat.domain.chat.model.response.ChatListResponse;
import com.example.OnlineOpenChat.domain.chat.model.response.CreateRoomResponse;
import com.example.OnlineOpenChat.domain.chat.model.response.JoinedRoomListResponse;
import com.example.OnlineOpenChat.domain.chat.mongo.document.ChatMessage;
import com.example.OnlineOpenChat.domain.chat.mongo.service.ChatMessageService;
import com.example.OnlineOpenChat.domain.repository.RoomMemberRepository;
import com.example.OnlineOpenChat.domain.repository.RoomRepository;
import com.example.OnlineOpenChat.domain.repository.UserRepository;
import com.example.OnlineOpenChat.domain.repository.entity.User;
import com.example.OnlineOpenChat.global.redis.RedisMessage;
import com.example.OnlineOpenChat.global.redis.publisher.NotificationRedisPublisher;
import com.example.OnlineOpenChat.security.auth.JWTProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatServiceV1 {

    private final RoomMemberRepository roomMemberRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    private final NotificationRedisPublisher notificationRedisPublisher;
    private final ChatMessageService chatMessageService;

    /**
     * 새로운 채팅 방 생성
      * @param authString
     * @param request
     * @return
     */
    @Transactional
    public CreateRoomResponse createRoom(String authString, CreateRoomRequest request) {
        
        try {
            // 1) 새로운 방 생성
            Room room = new Room(request.roomName());
            Room savedRoom = roomRepository.save(room);

            for (Long inviteeId : request.participantIds()) {
                User invitee = userRepository.findById(inviteeId)
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_USER));

                RoomMember inviteeMember = userToRoomMember(invitee, savedRoom.getId());
                roomMemberRepository.save(inviteeMember);
            }

            // 3) 방 멤버로 초대된 유저들에게 채팅방 구독 요청 알림 발송
            RedisMessage notification = RedisMessage.builder()
                    .type(RedisMessageType.INVITE)
                    .targetUserIds(request.participantIds())
                    .roomId(savedRoom.getId())
                    .roomName(request.roomName())
                    .build();

            notificationRedisPublisher.publishNotification(notification);

            // 4) 생성된 방 정보 반환
            return new CreateRoomResponse(ErrorCode.SUCCESS, request.roomName(), savedRoom.getId());

        } catch (CustomException e) {
            return new CreateRoomResponse(ErrorCode.NOT_EXIST_USER, null, null);
        } catch (Exception e) {
            return new CreateRoomResponse(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }

    }

    private RoomMember userToRoomMember(User user, Long roomId) {
        return new RoomMember(roomId, user.getId());
    }

    /**
     * UserId로 참여중인 채팅방 목록 조회
     * @param authString
     * @return
     */
    public JoinedRoomListResponse getJoinedRoomsByUserId(String authString) {
        try {
            Long userId = JWTProvider.getUserIdAsLong(authString);

            List<RoomDto> roomDtoList = roomRepository.findRoomsByUserId(userId);
            return new JoinedRoomListResponse(ErrorCode.SUCCESS, roomDtoList);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, null);
        }

    }

    /**
     * 채팅방의 최근 메시지 100개 조회
     * @param roomId
     * @return
     */
    public ChatListResponse getRecentMessagesInRoom(Long roomId) {
        try {
            List<ChatMessage> chatMessages = chatMessageService.getRecentMessages(roomId);
            return new ChatListResponse(ErrorCode.SUCCESS, chatMessages);
        } catch (Exception e) {
            log.error("채팅방 최근 메시지 조회 실패", e);
            return new ChatListResponse(ErrorCode.INTERNAL_SERVER_ERROR, null);
        }
    }

}

