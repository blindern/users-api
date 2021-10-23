FROM openjdk:11-jre-slim@sha256:9223f8e5033f47c86323d31aa98a3779c4edd89d92194287f04e2f5391fd9ac1

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
