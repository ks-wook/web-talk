# =========================
# 1단계: 빌드
# =========================
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

COPY . .
RUN gradle clean bootJar --no-daemon

# =========================
# 2단계: 런타임
# =========================
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/build/libs/app.jar app.jar

EXPOSE 7002
ENTRYPOINT ["java", "-jar", "app.jar"]
