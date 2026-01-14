package com.example.chat.repository;

import com.example.chat.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);

    // 닉네임으로 검색 -> 닉네임의 경우 중복 허용 안함
    Optional<User> findByNickname(String nickname);
    boolean existsByNickname(String nickname);


    // user(본인) 값을 제외한 패턴에 일치하는 닉네임들을 검색한다. - (deprecated) Locate 특성상 Index 탈수가 없음
    @Query("SELECT u.nickname FROM User AS u WHERE LOCATE(LOWER(:pattern), LOWER(u.nickname)) > 0 AND u.nickname != :user")
    List<String> findNameByNicknameMatch(@Param("pattern") String pattern, @Param("user") String user);

    // 닉네임이 특정 패턴으로 시작하는 닉네임들을 검색한다. 단, 본인 닉네임은 제외
    @Query("""
        SELECT u.nickname
        FROM User u
        WHERE u.nickname LIKE CONCAT(:pattern, '%')
          AND u.nickname <> :user
    """)
    List<String> findNameByNicknameMatchV2(String pattern, String user);

}
