package com.example.chat.service;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.*;

/**
 * WebSocket 메시지 전송 큐
 * <p>
 * 비동기적으로 WebSocket 메시지를 전송하기 위해
 * 별도의 큐와 스레드 풀을 사용
 */
@Component
public class WebSocketSendQueue {

    private static final Logger logger =
            LoggerFactory.getLogger(WebSocketSendQueue.class);

    private final BlockingQueue<WebSocketSendJob> queue =
            new LinkedBlockingQueue<>();

    private final ExecutorService senderExecutor;

    /**
     * 생성자에서 스레드 풀 설정, 작업 시작
     */
    public WebSocketSendQueue() {

        int cores = Runtime.getRuntime().availableProcessors();

        this.senderExecutor = new ThreadPoolExecutor(
                cores,
                cores * 2,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                r -> {
                    Thread t = new Thread(r);
                    t.setName("ws-send-worker");
                    t.setDaemon(true);
                    return t;
                }
        );

        // 메시지 전송작업 시작
        startConsumer();
    }

    /**
     * 큐에 전송 작업 추가
     * @param session
     * @param message
     */
    public void enqueue(WebSocketSession session, TextMessage message) {
        if (session == null || message == null) return;
        queue.offer(new WebSocketSendJob(session, message));
    }

    private void startConsumer() {
        senderExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    WebSocketSendJob item = queue.take();
                    send(item.session(), item.message());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    logger.error("Send failed", e);
                }
            }
        });
    }

    private void send(WebSocketSession session, TextMessage message) {
        if (!session.isOpen()) return;

        try {
            session.sendMessage(message);
        } catch (Exception e) {
            logger.warn(
                    "WebSocket send failed. sessionId={}",
                    session.getId(),
                    e
            );
        }
    }

    @PreDestroy
    public void shutdown() {
        senderExecutor.shutdown();
    }
}
