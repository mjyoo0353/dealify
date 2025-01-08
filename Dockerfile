#멀티 스테이지 빌드
#빌드 환경과 실행 환경을 분리하여 이미지 크기를 줄일 수 있음
#빌드 환경에서는 빌드에 필요한 것만 설치하고, 실행 환경에서는 빌드된 파일만 복사하여 사용

# Step 1: Build Stage
#Gradle과 JDK 21을 사용하여 애플리케이션 빌드
FROM gradle:8.11.1-jdk21 AS build
# 작업 디렉토리 설정
WORKDIR /app
# 프로젝트 파일을 컨테이너로 복사
COPY . /app
# Gradle 빌드를 실행하여 JAR 파일 생성
RUN ./gradlew bootJar

# Step 2: Run Stage
#alpine 또는 slim버전으로 사용하기
FROM openjdk:21-jdk-slim
# 작업 디렉토리 설정
WORKDIR /app
# 빌드 단계에서 생성된 JAR 파일을 실행 단계로 복사
COPY --from=build /app/build/libs/*.jar /app/myapp.jar
# JAR 파일 실행 명령어 지정
ENTRYPOINT ["java", "-jar", "/app/myapp.jar"]



## 1. 베이스 이미지 지정
#FROM openjdk:21-jdk-slim
## 2. 작업 디렉토리 설정
#WORKDIR /app
## 3. 빌드된 JAR 파일을 컨테이너로 복사
#COPY build/libs/*.jar /app/myapp.jar
## 4. JAR 파일 실행 명령어 지정
#ENTRYPOINT ["java","-jar","/app/myapp.jar"]