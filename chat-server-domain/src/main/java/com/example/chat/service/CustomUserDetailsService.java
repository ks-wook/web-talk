package com.example.chat.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface CustomUserDetailsService extends UserDetailsService {

    /**
     * 사용자 아이디(username)로 사용자 인증 정보 조회
     *
     * @param username 로그인 아이디
     * @return Spring Security UserDetails
     * @throws UsernameNotFoundException 사용자가 존재하지 않는 경우
     */
    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

}
