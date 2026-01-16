# web-talk

## DDL

### auth_refresh_token
* jwt RefreshToken 관리 테이블
```
CREATE TABLE auth_refresh_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_t_id BIGINT NOT NULL, -- user 테이블의 t_id 값
    refresh_token VARCHAR(255) NOT NULL, -- jwt refreshToken 값
    expired_at TIMESTAMP NOT NULL, -- 만료 일자 ex) 1일, 7일...
    is_revoked TINYINT(1) NOT NULL DEFAULT 0, -- 강제 만료 처리(로그 아웃)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```
### user, user_credentials
* 유저 정보 관리 테이블
```
CREATE table user_credentials
(
    user_t_id       BIGINT   null, -- 유저 고유의 ID 값, 유저 테이블의 t_id 값
    hashed_password char(60) not null, -- 해싱된 유저 비번
    constraint user_t_id UNIQUE (user_t_id)
);

CREATE table user
(
    t_id        BIGINT AUTO_INCREMENT PRIMARY KEY   primary key,
    login_id    VARCHAR(100)                        not null,
    created_at  TIMESTAMP default CURRENT_TIMESTAMP null,
    nickname    VARCHAR(255)                        null, -- 닉네임
    status_text VARCHAR(255)                        null, -- 상태메시지
    
    KEY idx_user_nickname (nickname)
);
```

### friend
* 친구 관계 관리 테이블
```
CREATE TABLE friends (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '친구를 추가한 사용자 ID',
    friend_id BIGINT NOT NULL COMMENT '추가된 친구의 사용자 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### room, room_member
* 채팅방 및 채팅방 멤버 관리 테이블
```
CREATE TABLE rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NULL COMMENT '방 이름 (선택사항)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE room_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL COMMENT '방 ID',
    user_id BIGINT NOT NULL COMMENT '참여자 사용자 ID',
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```


