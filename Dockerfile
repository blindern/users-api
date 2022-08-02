FROM eclipse-temurin:18-jre-alpine@sha256:ed306c8e55b71f38d8e9c2b218afd58d9146f503b5f9a9f0189c80927567c38d

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

COPY build/build.properties /build.properties

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
