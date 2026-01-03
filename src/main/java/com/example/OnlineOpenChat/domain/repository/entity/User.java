package com.example.OnlineOpenChat.domain.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "t_id")
    private Long id;

    /**
     * 로그인 아이디
     */
    @Column(name = "login_id", nullable = false)
    private String loginId;

    /**
     * UI에 표시될 유저 닉네임
     */
    @Column(nullable = false)
    private String nickname;

    /**
     * 유저 상태 메시지
     */
    @Column(name="status_text", nullable = false)
    private String statusText;

    @Column
    private Timestamp created_at;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserCredentials userCredentials;

    public void setCredentials(UserCredentials credentials) {
        this.userCredentials = credentials;
    }
}
