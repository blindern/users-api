FROM eclipse-temurin:21.0.5_11-jre-alpine@sha256:4300bfe1e11f3dfc3e3512f39939f9093cf18d0e581d1ab1ccd0512f32fe33f0

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

COPY build/build.properties /build.properties

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
