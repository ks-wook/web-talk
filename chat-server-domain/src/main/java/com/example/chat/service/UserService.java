package com.example.chat.service;

import com.example.chat.model.request.AddFriendRequest;
import com.example.chat.model.request.UpdateNicknameRequest;
import com.example.chat.model.request.UpdateStatusTextRequest;
import com.example.chat.model.response.*;

public interface UserService {

    /**
     * 유저 검색 처리
     *
     * @param nickname 검색할 닉네임
     * @param myName   본인 닉네임 (검색 제외용)
     * @return 유저 검색 결과
     */
    UserSearchResponse searchUser(String nickname, String myName);

    /**
     * 친구 추가 처리
     *
     * @param authString AccessToken
     * @param request    친구 추가 요청
     * @return 친구 추가 결과
     */
    AddFriendResponse addFriend(String authString, AddFriendRequest request);

    /**
     * 친구 목록 조회
     *
     * @param authString AccessToken
     * @return 친구 목록
     */
    GetFriendListResponse getFriendListByUserId(String authString);

    /**
     * 상태 메시지 수정 처리
     *
     * @param authString AccessToken
     * @param request    상태 메시지 수정 요청
     * @return 상태 메시지 수정 결과
     */
    UpdateStatusTextResponse updateStatusText(
            String authString,
            UpdateStatusTextRequest request
    );

    /**
     * 닉네임 변경 처리
     *
     * @param authString AccessToken
     * @param request    닉네임 변경 요청
     * @return 닉네임 변경 결과
     */
    UpdateNicknameResponse updateNickname(
            String authString,
            UpdateNicknameRequest request
    );
}
