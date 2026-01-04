package com.example.OnlineOpenChat.domain.user.controller;

import com.example.OnlineOpenChat.domain.user.model.request.AddFriendRequest;
import com.example.OnlineOpenChat.domain.user.model.request.UpdateNicknameRequest;
import com.example.OnlineOpenChat.domain.user.model.request.UpdateStatusTextRequest;
import com.example.OnlineOpenChat.domain.user.model.response.*;
import com.example.OnlineOpenChat.domain.user.service.UserServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User API", description = "V1 User API")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserControllerV1 {

    private final UserServiceV1 userServiceV1;

    @Operation(
            summary = "User Name List Search",
            description = "User Name을 기반으로 Like 검색 실행"
    )
    @GetMapping("/search/{name}")
    public UserSearchResponse searchUser(
            @RequestHeader("Authorization") String authString,
            @PathVariable("name") String nickname,
            @RequestParam(name = "myName") String myName
    ) {
        return userServiceV1.searchUser(nickname, myName);
    }
    
    @Operation(
            summary = "친구 추가 요청 처리",
            description = "요청 받은 유저를 해당 유저의 친구 목록에 추가합니다."
    )
    @PostMapping("/add-friend")
    public AddFriendResponse addFriend(
            @RequestHeader("Authorization") String authString,
            @RequestBody AddFriendRequest request
    ) {
        return userServiceV1.addFriend(authString, request);
    }

    @Operation(
            summary = "친구 목록 조회",
            description = "해당 유저의 친구 목록을 반환합니다."
    )
    @GetMapping("/get-friendList")
    public GetFriendListResponse getFriends(
            @RequestHeader("Authorization") String authString
    ) {
        return userServiceV1.getFriendListByUserId(authString);
    }

    @Operation(
            summary = "상태메시지 변경 요청",
            description = "유저의 상태메시지를 변경합니다."
    )
    @PostMapping("/update-status-text")
    public UpdateStatusTextResponse updateStatusMessage(
            @RequestHeader("Authorization") String authString,
            @RequestBody UpdateStatusTextRequest request
            ) {
        return userServiceV1.updateStatusText(authString, request);
    }

    @Operation(
            summary = "유저 닉네임 변경 요청",
            description = "유저의 닉네임을 변경합니다."
    )
    @PostMapping("/update-nickname")
    public UpdateNicknameResponse updateNickname(
            @RequestHeader("Authorization") String authString,
            @RequestBody UpdateNicknameRequest request
    ) {
        return userServiceV1.updateNickname(authString, request);
    }
}
