package com.example.chat.repository;

import com.example.chat.model.entity.Friend;
import com.example.chat.dto.FriendDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findByUserId(Long userId);

    /**
     * 친구 목록 조회 (닉네임 포함)
     */
    @Query("""
        select new com.example.chat.dto.FriendDto(
            u.id,
            u.nickname,
            u.statusText
        )
        from Friend f
        join User u on f.friendId = u.id
        where f.userId = :userId
    """)
    List<FriendDto> findFriendDto(@Param("userId") Long userId);

    /**
     * 특정 유저의 친구 관계 존재 여부 확인
     */
    boolean existsByUserIdAndFriendId(Long userId, Long friendId);
}