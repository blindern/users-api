FROM eclipse-temurin:21.0.4_7-jre-alpine@sha256:848653d62f2fe03f2ef6d0527236fbdedd296ee44bfeb7a9836662dc2f0f58d3

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

COPY build/build.properties /build.properties

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
