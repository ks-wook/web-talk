package com.example.OnlineOpenChat.domain.auth.service;

import com.example.OnlineOpenChat.common.exception.CustomException;
import com.example.OnlineOpenChat.common.exception.ErrorCode;
import com.example.OnlineOpenChat.domain.auth.model.request.CreateUserRequest;
import com.example.OnlineOpenChat.domain.auth.model.request.LoginRequest;
import com.example.OnlineOpenChat.domain.auth.model.response.CreateUserResponse;
import com.example.OnlineOpenChat.domain.auth.model.response.GetMyInfoResponse;
import com.example.OnlineOpenChat.domain.auth.model.response.LoginResponse;
import com.example.OnlineOpenChat.domain.repository.AuthRefreshTokenRepository;
import com.example.OnlineOpenChat.domain.repository.UserRepository;
import com.example.OnlineOpenChat.domain.repository.entity.AuthRefreshToken;
import com.example.OnlineOpenChat.domain.repository.entity.User;
import com.example.OnlineOpenChat.domain.repository.entity.UserCredentials;
import com.example.OnlineOpenChat.security.auth.JWTProvider;
import com.example.OnlineOpenChat.util.CookieUtil;
import com.example.OnlineOpenChat.util.DateUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthRefreshTokenRepository authRefreshTokenRepository;

    private final PasswordEncoder passwordEncoder;

    /*
     * 유저 회원 가입 처리
     */
    @Transactional
    public CreateUserResponse createUser(CreateUserRequest request) {
        log.info("유저 회원가입 요청: UserId {}", request.loginId());

        try {
            Optional<User> user = userRepository.findByLoginId(request.loginId());

            // 1) 이미 등록된 유저 닉네임인지 검사
            if(user.isPresent()) {
                log.error("USER_ALREADY_EXISTS: {}", request.loginId());
                throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
            }

            // 2) 유저 정보 등록
            // 유저의 기본 닉네임은 "UserName" + #{랜덤숫자4자리} 형태로 지정
            String defaultNickname = "UserName" + (int)(Math.random() * 9000) + 1000;
            User newUser = this.newUser(request.loginId(), defaultNickname);

            UserCredentials newCredentials = this.newUserCredentials(request.password(), newUser);
            newUser.setCredentials(newCredentials);

            userRepository.save(newUser);

            // 3) 회원가입 성공
            log.info("회원가입 성공 UserId : {}", newUser.getLoginId());
            return new CreateUserResponse(ErrorCode.SUCCESS, (request.loginId()));
        }
        catch (CustomException e) {
            log.error("회원가입 실패: {}", e.getMessage());
            return new CreateUserResponse(e.getErrorCode(), null);
        }
        catch (Exception e) {
            throw new CustomException(ErrorCode.USER_SAVED_FAILED);
        }
    }

    /**
     * 유저 로그인 처리
     * @param request
     * @return
     */
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        log.info("유저 로그인 요청: UserId {}", request.loginId());

        try {
            Optional<User> user = userRepository.findByLoginId((request.loginId()));

            // 유저정보 없는경우
            if(user.isEmpty()) {
                log.error("NOT_EXIST_USER: {}", request.loginId());
                throw new CustomException(ErrorCode.NOT_EXIST_USER);
            }

            // 로그인 정보 유효한지 검증
            // Optional로 감싸진 객체에 대해서 map은 객체가 존재할때만 실행
            User u = user.orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_USER));

            if (!passwordEncoder.matches(request.password(), u.getUserCredentials().getHashed_password())) {
                throw new CustomException(ErrorCode.MIS_MATCH_PASSWORD);
            }

            // 로그인 성공 - JWT 토큰 발급 -> Subject: userId Long 타입 아이디값
            String accessToken = JWTProvider.createAccessToken(String.valueOf(user.get().getId()));
            String refreshToken = JWTProvider.createRefreshToken();

            // RefreshToken 만료일자
            Date refreshTokenExpiredAt = JWTProvider.getRefreshTokenExpiredAtFromNow();

            // refreshToken을 db에 저장
            AuthRefreshToken authRefreshToken = AuthRefreshToken.builder()
                    .userId(user.get().getId())
                    .refreshToken(refreshToken)
                    .expiredAt(DateUtil.dateToLocalDateTime(refreshTokenExpiredAt))
                    .isRevoked(false)
                    .build();


            // 1) 발급된 refreshToken을 DB에 영속화
            authRefreshTokenRepository.save(authRefreshToken);

            // 2) Client로 돌려줄 응답에 쿠키값 세팅
            CookieUtil.addRefreshTokenCookie(response, refreshToken, refreshTokenExpiredAt);

            // 3) 로그인 성공 응답 반환 + 발급된 accessToken을 포함
            return new LoginResponse(ErrorCode.SUCCESS, accessToken, user.get().getNickname());

        } catch (CustomException e) {
            log.warn("로그인 실패: {}", e.getMessage());
            return new LoginResponse(e.getErrorCode(), null, null);
        } catch (Exception e) {
            return new LoginResponse(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * AccessToken을 통해 내 정보 조회
     * @param authString
     * @return
     */
    public GetMyInfoResponse getMyInfoByAccessToken (String authString) {
        try {

            Long userId = JWTProvider.getUserIdAsLong(authString);
            log.info("Get My Info 요청 UserId: {}", userId);
            if(userId == null) {
                throw new CustomException(ErrorCode.TOKEN_IS_INVALID);
            }

            Optional<User> user = userRepository.findById(userId);
            log.info("Get My Info User: {}", user);

            if(user.isEmpty()) {
                throw new CustomException(ErrorCode.NOT_EXIST_USER);
            }

            return new GetMyInfoResponse(ErrorCode.SUCCESS, user.get().getId(), user.get().getNickname());

        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, null);
        }

    }

    /**
     * 토큰에서 유저 아이디 추출
     * @param token
     * @return
     */
    public String getUserFromToken(String token) {
        return JWTProvider.getUserId(token);
    }

    /**
     * 새 유저 생성
     * @param name
     * @return
     */
    private User newUser(String name, String defaultNickname) {
        return User.builder()
                .loginId(name)
                .nickname(defaultNickname)
                .created_at(new Timestamp(System.currentTimeMillis()))
                .build();
    }

    /**
     * 유저 암호화 정보 생성
     * @param password
     * @param user
     * @return
     */
    private UserCredentials newUserCredentials(String password, User user) {
        String hashingValue = passwordEncoder.encode(password);

        return UserCredentials
                .builder()
                .user(user)
                .hashed_password(hashingValue)
                .build();
    }

}
