FROM openjdk:11-jre-slim@sha256:2b0f112d35f2150ec160d6d7b1158a982519900ba5d118fb803dfe1d0371b0d4

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
