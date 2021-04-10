FROM openjdk:11-jre-slim@sha256:efd4c4c82743e427c22a1ba2aa81852d730a4e8c0589248608b4f6bf7087314f

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
