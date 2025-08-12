# Java 11이 설치된 경량 베이스 이미지 사용
FROM openjdk:11-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일을 컨테이너에 복사
COPY target/myShop-0.0.1-SNAPSHOT.jar app.jar

# 외부로 열어줄 포트
EXPOSE 8080

# 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]