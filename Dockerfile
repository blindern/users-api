FROM openjdk:11-jre-slim@sha256:2bef450a187db58e26e13c1c6d73e28d15f6163867c116f2574ec4e6a23a00b5

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
