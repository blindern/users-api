FROM openjdk:11-jre-slim@sha256:f7971e32b15b5f19e1ad149bd0ad9528042c4c6e4747bf2eb8008de097784ed7

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
