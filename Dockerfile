FROM openjdk:11-jre-slim@sha256:b4cb057e8e7f534c3ae279b5f3205e28d6520c0bdb1f80bbf61c0b8d2c00f983

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
