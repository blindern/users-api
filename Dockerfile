FROM openjdk:11-jre-slim@sha256:7fc50edeefdda41ab1e3868e9aaa103561f39f39a742fff4b6b3786736f0c0d0

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
