FROM eclipse-temurin:19-jre-alpine@sha256:56b086d58ab1c5b70c730e9ccd82e18a3e12db87f9351f2c514579e58a6ba579

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

COPY build/build.properties /build.properties

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
