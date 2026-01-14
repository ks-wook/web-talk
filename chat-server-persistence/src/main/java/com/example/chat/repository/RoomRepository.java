package com.example.chat.repository;

import com.example.chat.model.entity.Room;
import com.example.chat.dto.RoomDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByName(String name);
    
    /**
     * UserId 값을 기반으로 유저가 참여중인 채팅방들 조회
     */
    @Query("""
        select new com.example.chat.dto.RoomDto(
            r.id,
            r.name
        )
        from Room r
        join r.members m
        where m.userId = :userId
    """)
    List<RoomDto> findRoomsByUserId(@Param("userId") Long userId);
}
