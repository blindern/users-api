FROM openjdk:11-jre-slim@sha256:b4f3ffe87eee841f553b9491eaa365e33c6e9bdf0656499733abb5107810aa11

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
