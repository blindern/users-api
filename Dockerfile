FROM eclipse-temurin:18-jre-alpine@sha256:111984299906a2216ae56eb7829029b04a003be52e90e6117b65824e547a5edc

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

COPY build/build.properties /build.properties

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
