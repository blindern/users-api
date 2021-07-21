FROM openjdk:11-jre-slim@sha256:582af62360b688c9ff4b4c5838c7d6b8eae55fbf4c57e90011e4a9c6eeb0746a

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
