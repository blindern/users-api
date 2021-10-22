FROM openjdk:11-jre-slim@sha256:88db4db65f6c8fc261cdbb12595d1f99dddc2c1c3248fee95d76fa128b1c6dab

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
