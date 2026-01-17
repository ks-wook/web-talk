package com.example.chat.service;

import com.example.chat.common.exception.CustomException;
import com.example.chat.common.exception.ErrorCode;
import com.example.chat.model.request.CreateUserRequest;
import com.example.chat.model.request.LoginRequest;
import com.example.chat.model.response.*;
import com.example.chat.repository.AuthRefreshTokenRepository;
import com.example.chat.repository.UserRepository;
import com.example.chat.model.entity.AuthRefreshToken;
import com.example.chat.model.entity.User;
import com.example.chat.model.entity.UserCredentials;
import com.example.chat.security.auth.JWTProvider;
import com.example.chat.util.CookieUtil;
import com.example.chat.util.DateUtil;
import jakarta.servlet.http.HttpServletRequest;
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
public class AuthServiceImpl implements AuthService {

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
            String defaultNickname = request.nickname() + "#" + (int)(Math.random() * 9000) + 1000;
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
            log.error("회원가입 실패: {}", e.getMessage());
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

            Optional<User> user = userRepository.findById(userId);

            if(user.isEmpty()) {
                throw new CustomException(ErrorCode.NOT_EXIST_USER);
            }

            return new GetMyInfoResponse(ErrorCode.SUCCESS, user.get().getId(), user.get().getNickname(), user.get().getStatusText());

        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, null);
        }

    }

    /**
     * 로그아웃 처리
     * @param authString
     * @param httpServletRequest
     * @param httpServletResponse
     */
    public LogoutResponse logout(String authString, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        try {
            Long userId = JWTProvider.getUserIdAsLong(authString);

            String RefreshToken = CookieUtil.getRefreshTokenFromRequest(httpServletRequest);

            // DB에 저장된 RefreshToken 만료 처리
            Optional<AuthRefreshToken> refreshToken = authRefreshTokenRepository.findByRefreshToken(RefreshToken);

            if(refreshToken.isPresent()) {
                AuthRefreshToken token = refreshToken.get();
                token.setIsRevoked(true);
                authRefreshTokenRepository.save(token);
            }
            else {
                throw new CustomException(ErrorCode.REFRESH_TOKEN_IS_NOT_FOUND, "로그아웃 처리할 RefreshToken이 존재하지 않습니다.");
            }

            // 클라이언트 쿠키에 저장된 RefreshToken 삭제 처리
            CookieUtil.deleteRefreshTokenCookie(httpServletResponse);

            return new LogoutResponse(ErrorCode.SUCCESS);

        } catch (CustomException e) {
            throw new CustomException(e.getErrorCode());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Access Token 재발급
     * @param request
     * @return
     */
    @Transactional
    public ReissueAccessTokenResponse reissueAccessToken(HttpServletRequest request) {

        try {
            // httpRequest의 쿠키값을 통해 RequestToken 추출
            String refreshToken = CookieUtil.getRefreshTokenFromRequest(request);

            if (refreshToken == null) {
                log.info("[AuthServiceImpl] Refresh Token cookie not found.");
                throw new CustomException(ErrorCode.REFRESH_TOKEN_IS_NOT_FOUND, "Refresh Token이 존재하지 않습니다.");
            } else {
                log.info("[AuthServiceImpl] Refresh Token from cookie: {}", refreshToken);
            }

            // Service 로직에서 DB 테이블 확인해서 RefreshToken 검증 후 AccessToken 재발급
            Optional<AuthRefreshToken> searchedRefreshToken = authRefreshTokenRepository.findByRefreshToken(refreshToken);

            // refreshToken이 존재하지 않거나, 이미 폐기된 토큰인 경우
            if(searchedRefreshToken.isEmpty() || searchedRefreshToken.get().getIsRevoked()) {
                return new ReissueAccessTokenResponse(ErrorCode.TOKEN_IS_INVALID, null);
            }
            else if(searchedRefreshToken.get().getExpiredAt().isBefore(DateUtil.dateToLocalDateTime(new Date()))) {
                // dirty check - 만료된 토큰인 경우
                // isRevoked 필드 true로 변경 -> 스케줄러는 매일 utc 기준 자정에 만료된 토큰 정리 작업 수행
                // but, 만료된 토큰처리가 되지 않은 상태에서 reissue 요청이 들어올 수 있으므로 여기서도 처리
                searchedRefreshToken.get().setIsRevoked(true);

                return new ReissueAccessTokenResponse(ErrorCode.TOKEN_IS_INVALID, null);
            }
            else { // 토큰이 유효한 경우
                Long userId = searchedRefreshToken.get().getUserId();

                // 새로운 Access Token 발급
                String newAccessToken = JWTProvider.createAccessToken(String.valueOf(userId));

                return new ReissueAccessTokenResponse(ErrorCode.SUCCESS, newAccessToken);
            }
        }
        catch (Exception e) {
            return new ReissueAccessTokenResponse(ErrorCode.INTERNAL_SERVER_ERROR, null);
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
     * @param loginId
     * @return
     */
    private User newUser(String loginId, String defaultNickname) {
        return User.builder()
                .loginId(loginId)
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
