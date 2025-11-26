package com.example.OnlineOpenChat.domain.auth.service;

import com.example.OnlineOpenChat.common.exception.CustomException;
import com.example.OnlineOpenChat.common.exception.ErrorCode;
import com.example.OnlineOpenChat.domain.auth.model.request.CreateUserRequest;
import com.example.OnlineOpenChat.domain.auth.model.request.LoginRequest;
import com.example.OnlineOpenChat.domain.auth.model.response.CreateUserResponse;
import com.example.OnlineOpenChat.domain.auth.model.response.LoginResponse;
import com.example.OnlineOpenChat.domain.repository.UserRepository;
import com.example.OnlineOpenChat.domain.repository.entity.User;
import com.example.OnlineOpenChat.domain.repository.entity.UserCredentials;
import com.example.OnlineOpenChat.security.Hasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final Hasher hasher;

    /*
     * 유저 회원 가입 처리
     */
    @Transactional(transactionManager = "createUserTransactionManager")
    public CreateUserResponse createUser(CreateUserRequest request) {
        Optional<User> user = userRepository.findByName(request.name());
        
        if(user.isPresent()) {
            log.error("USER_ALREADY_EXISTS: {}", request.name());
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }
        
        try {

            User newUser = this.newUser(request.name());
            UserCredentials newCredentials = this.newUserCredentials(request.password(), newUser);
            newUser.setCredentials(newCredentials);

            // save 실패하면 내부에서 Exception 던진다.
            userRepository.save(newUser);
        }
        catch (Exception e) {
            throw new CustomException(ErrorCode.USER_SAVED_FAILED);
        }

        return new CreateUserResponse((request.name()));
    }

    /**
     * 유저 로그인 처리
     * @param request
     * @return
     */
    public LoginResponse login(LoginRequest request) {
        Optional<User> user = userRepository.findByName((request.name()));

        // 유저정보 없는경우
        if(user.isPresent()) {
            log.error("NOT_EXIST_USER: {}", request.name());
            throw new CustomException(ErrorCode.NOT_EXIST_USER);
        }

        // 로그인 정보 유효한지 검증
        // Optional로 감싸진 객체에 대해서 map은 객체가 존재할때만 실행
        user.map(u -> {
            String hashingValue = hasher.getHashingValue(request.password());

            if(!u.getUserCredentials().getHashed_password().equals((hashingValue))) {
                throw new CustomException(ErrorCode.MIS_MATCH_PASSWORD);
            }

            return hashingValue;

        }).orElseThrow(() -> new CustomException(ErrorCode.MIS_MATCH_PASSWORD));

        return new LoginResponse(ErrorCode.SUCCESS, "Token");
    }

    /**
     * 새 유저 생성
     * @param name
     * @return
     */
    private User newUser(String name) {
        return User.builder()
                .name(name)
                .create_at(new Timestamp(System.currentTimeMillis()))
                .build();
    }

    /**
     * 유저 암호화 정보 생성
     * @param password
     * @param user
     * @return
     */
    private UserCredentials newUserCredentials(String password, User user) {
        String hashingValue = hasher.getHashingValue(password);

        return UserCredentials
                .builder()
                .user(user)
                .hashed_password(hashingValue)
                .build();
    }

}
