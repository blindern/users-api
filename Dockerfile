FROM eclipse-temurin:21.0.3_9-jre-alpine@sha256:11cb6264789abec7478d085b7bd3f7263dbea23ca261c795c03ade9d4d449ff4

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

COPY build/build.properties /build.properties

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
