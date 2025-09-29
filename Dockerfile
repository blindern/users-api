FROM eclipse-temurin:25-jre-alpine@sha256:0be0f9b8c4c9c55bd97194ed84f0751f639f5712d9e99a45a0d65bf49f57bd08

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

COPY build/build.properties /build.properties

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
