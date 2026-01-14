# =========================
# 1단계: 빌드
# =========================
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Gradle 설정 먼저 복사 (캐시 활용)
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 멀티 모듈이므로 전체 의존성 확인
RUN gradle dependencies --no-daemon || true

# 전체 소스 복사
COPY . .

# chat-server-application 모듈만 bootJar 생성
RUN gradle :chat-server-application:clean \
           :chat-server-application:bootJar \
           --no-daemon

# =========================
# 2단계: 런타임
# =========================
FROM eclipse-temurin:17-jre
WORKDIR /app

# 실행 모듈의 jar만 복사
COPY --from=build /app/chat-server-application/build/libs/*.jar app.jar

EXPOSE 7002
ENTRYPOINT ["java", "-jar", "app.jar"]