FROM openjdk:11-jre-slim

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
