FROM openjdk:11-jre-slim@sha256:28b59dc9a129c75349418e3b75508fd8eef3b33dd7d796079d1d19445d907776

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
