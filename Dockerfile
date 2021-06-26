FROM openjdk:11-jre-slim@sha256:24de726604f496a8d34cc960f39c3f3d825ebba522d8be7b256f8b289a448508

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
