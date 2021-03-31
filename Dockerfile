FROM openjdk:11-jre-slim@sha256:57b48424af7456fef1e2b0f377fffa76ac57270a63ee780cdcbd5c6610595791

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
