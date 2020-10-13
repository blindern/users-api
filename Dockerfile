FROM openjdk:11-jre-slim@sha256:450ec7968dfbce85b26c3581cfd38816e1b03617fbb8b91ae6f365dd37244807

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
