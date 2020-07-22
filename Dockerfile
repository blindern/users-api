FROM openjdk:11-jre-slim@sha256:e51aee6fa026805279a2a4bba9db0d0c4ef53d65b39d33d34e32d13a5f6970b4

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
