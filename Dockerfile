FROM eclipse-temurin:18-jre-alpine@sha256:49d8b2132b2c8d0c13685307e39c1f21841276eab5ae0fc511bedaacda8d86fd

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

COPY build/build.properties /build.properties

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
