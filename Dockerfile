FROM openjdk:11-jre-slim@sha256:aab9aa5b80352a541b32456dd85d122efe6dc22da65f05405d2a6d681ad46389

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
