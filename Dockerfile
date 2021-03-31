FROM openjdk:11-jre-slim@sha256:6dfb830a52da0f7f40b4be1dfabdc3483026b89ff1c2c1413b75b6aa9e086852

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
