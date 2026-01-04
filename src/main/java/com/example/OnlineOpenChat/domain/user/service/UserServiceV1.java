package com.example.OnlineOpenChat.domain.user.service;

import com.example.OnlineOpenChat.common.exception.CustomException;
import com.example.OnlineOpenChat.common.exception.ErrorCode;
import com.example.OnlineOpenChat.domain.repository.UserRepository;
import com.example.OnlineOpenChat.domain.repository.entity.User;
import com.example.OnlineOpenChat.domain.user.model.Friend;
import com.example.OnlineOpenChat.domain.user.model.FriendDto;
import com.example.OnlineOpenChat.domain.user.model.request.AddFriendRequest;
import com.example.OnlineOpenChat.domain.user.model.request.UpdateNicknameRequest;
import com.example.OnlineOpenChat.domain.user.model.request.UpdateStatusTextRequest;
import com.example.OnlineOpenChat.domain.user.model.response.*;
import com.example.OnlineOpenChat.domain.user.repository.FriendRepository;
import com.example.OnlineOpenChat.security.auth.JWTProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceV1 {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    /**
     * 유저 검색 처리
     * @param nickname
     * @param myName
     * @return
     */
    public UserSearchResponse searchUser(String nickname, String myName) {

        try {
            List<String> userList = userRepository.findNameByNicknameMatch(nickname, myName);

            return new UserSearchResponse(ErrorCode.SUCCESS, userList);
        } catch (CustomException e) {
            log.error("유저 검색 중 오류 발생: {}", e.getMessage());
            return new UserSearchResponse(e.getErrorCode(), null);
        } catch (Exception e) {
            log.error("유저 검색 중 알 수 없는 오류 발생: {}", e.getMessage());
            return new UserSearchResponse(ErrorCode.INTERNAL_SERVER_ERROR, null);
        }
    }

    /**
     * 친구 추가 처리
     * @param authString
     * @param request
     * @return
     */
    public AddFriendResponse addFriend(String authString, AddFriendRequest request) {

        try {
            Long userId = JWTProvider.getUserIdAsLong(authString);

            // 닉네임을 통해 존재하는 유저인지 검색
            Optional<User> user = userRepository.findByNickname(request.friendNickname());

            if(user.isPresent()) {
                Friend newFriend = new Friend(userId, user.get().getId());
                
                // 이미 친구로 등록되어 있는 지 검사
                if(friendRepository.existsByUserIdAndFriendId(userId, user.get().getId())) {
                    throw new CustomException(ErrorCode.ALREADY_FRIEND, "이미 친구로 등록된 유저입니다.");
                }
                
                friendRepository.save(newFriend);
            } else {
                throw new CustomException(ErrorCode.NOT_EXIST_USER, "존재하지 않는 유저입니다.");
            }

            // 친구 추가 성공
            return new AddFriendResponse(ErrorCode.SUCCESS);
        } catch (CustomException e) {
            return new AddFriendResponse(e.getErrorCode());
        } catch (Exception e) {
            return new AddFriendResponse(ErrorCode.INTERNAL_SERVER_ERROR);
        }

    }
    
    /**
     * 친구 목록 조회
     * @param authString
     * @return
     */
    public GetFriendListResponse getFriendListByUserId(String authString) {
        try {
            Long userId = JWTProvider.getUserIdAsLong(authString);
            List<FriendDto> friendList = friendRepository.findFriendDto(userId);

            return new GetFriendListResponse(ErrorCode.SUCCESS, friendList);

        } catch (CustomException e) {
            return new GetFriendListResponse(e.getErrorCode(), null);
        } catch (Exception e) {
            return new GetFriendListResponse(ErrorCode.INTERNAL_SERVER_ERROR, null);
        }

    }

    /**
     * 상태 메시지 수정 처리
     * @param authString
     * @param reqeust
     * @return
     */
    @Transactional
    public UpdateStatusTextResponse updateStatusText(String authString, UpdateStatusTextRequest reqeust) {
        try {
            Long userId = JWTProvider.getUserIdAsLong(authString);

            User user = userRepository.findById(userId)
                    .orElseThrow(() ->
                            new CustomException(ErrorCode.NOT_EXIST_USER, "존재하지 않는 유저입니다.")
                    );

            // 상태 메시지 수정
            user.updateStatusText(reqeust.statusText());

            return new UpdateStatusTextResponse(ErrorCode.SUCCESS, reqeust.statusText());

        } catch (CustomException e) {
            return new UpdateStatusTextResponse(e.getErrorCode(), null);
        } catch (Exception e) {
            return new UpdateStatusTextResponse(ErrorCode.INTERNAL_SERVER_ERROR, null);
        }
    }

    @Transactional
    public UpdateNicknameResponse updateNickname(String authString, UpdateNicknameRequest reqeust) {
        try {
            Long userId = JWTProvider.getUserIdAsLong(authString);

            User user = userRepository.findById(userId)
                    .orElseThrow(() ->
                            new CustomException(ErrorCode.NOT_EXIST_USER, "존재하지 않는 유저입니다.")
                    );

            // 유저 구분을 위한 해시코드 붙이기
            String nickname = reqeust.newNickname() + "#" + String.format("%04d", (int)(Math.random() * 10000));

            // 닉네임 변경
            user.updateNickname(nickname);

            return new UpdateNicknameResponse(ErrorCode.SUCCESS, nickname);

        } catch (CustomException e) {
            log.error("닉네임 변경 중 오류 발생: {}", e.getMessage());
            return new UpdateNicknameResponse(e.getErrorCode(), null);
        } catch (Exception e) {
            log.error("닉네임 변경 중 알 수 없는 오류 발생: {}", e.getMessage());
            return new UpdateNicknameResponse(ErrorCode.INTERNAL_SERVER_ERROR, null);
        }
    }
}
