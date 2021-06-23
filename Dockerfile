FROM openjdk:11-jre-slim@sha256:550345cd237e42a5772b8eb9c76cb7020c62645de5da77649f821e3c556f38d1

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
