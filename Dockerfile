FROM openjdk:11-jre-slim@sha256:6e6687768b9f32e8a7fe7197bac0cf46ac559b0703f55faaba51f9b28d57db29

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
