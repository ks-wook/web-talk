package com.example.chat.redis.constants;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Constants {
    // redis 채팅 데이터 스트림 키
    public static final String CHAT_STREAM_KEY = "chat_stream";

    // redis 채팅 컨슈머 그룹명
    public static final String CHAT_CONSUMER_GROUP = "chat_consumer_group";
}
