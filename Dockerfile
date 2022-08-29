FROM eclipse-temurin:18-jre-alpine@sha256:ae4122ed93a30f14cb60f2a758357262b0ae47c3f4a043374a742e229a8e329d

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

COPY build/build.properties /build.properties

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
