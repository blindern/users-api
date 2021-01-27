FROM openjdk:11-jre-slim@sha256:d9a6b51ec836e6278521678640211544b93272a3df675c915ca01a78c8c18ecd

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
