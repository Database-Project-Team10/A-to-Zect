# 1. 빌드 단계 (Java 23 지원 Gradle 이미지 사용)
# 주의: Gradle 8.10 이상이어야 Java 23을 완벽하게 지원합니다.
FROM gradle:8.10-jdk23 AS builder
WORKDIR /app

# 소스 코드 복사
COPY . .

# 빌드 (테스트 제외)
RUN gradle clean build -x test --no-daemon

# 2. 실행 단계 (Java 23 JRE 이미지 사용)
FROM eclipse-temurin:23-jre-alpine
WORKDIR /app

# 빌드 단계에서 생성된 JAR 파일만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]