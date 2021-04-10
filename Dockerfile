FROM openjdk:11-jre-slim@sha256:85795599f4c765182c414a1eb4e272841e18e2f267ce5010ea6a266f7f26e7f6

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
