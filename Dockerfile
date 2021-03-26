FROM openjdk:11-jre-slim@sha256:b7fdff95f68e4999c2cb49d5511dfacb0eaaacfabc3fce1faaf87ecef5a36a4a

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
