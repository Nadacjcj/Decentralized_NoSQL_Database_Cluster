FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY build/libs/Test-0.0.1-SNAPSHOT.jar app.jar
COPY user_data /app/user_data

EXPOSE 9001

ENTRYPOINT ["java","-jar","app.jar"]
