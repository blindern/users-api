FROM eclipse-temurin:18-jre-alpine@sha256:a70449ceb702bbf8bfd698f15b7a6669201f2fe003a338f782aae76660ed0fc0

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

COPY build/build.properties /build.properties

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
