FROM eclipse-temurin:22.0.1_8-jre-alpine@sha256:92f3cd14681c9e56ad44f25628d3b289e0b463ed25be52f7c5dd29acec43db97

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

COPY build/build.properties /build.properties

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
