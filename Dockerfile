FROM openjdk:11-jre-slim@sha256:2dc2fe284c751c4578225c14afcadeba0be10e7fa627920272089afb83f2f6de

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
