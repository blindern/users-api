FROM eclipse-temurin:18-jre-alpine@sha256:ba9e2b6bd44f56dc32494a25209f56899380b6d2ff62e7037d143a7a9869f438

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

COPY build/build.properties /build.properties

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
