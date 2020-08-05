FROM openjdk:11-jre-slim@sha256:25ae9b4d57101dd9ea9d932941536b1f00b341c3425fc5f8289898d191254447

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
