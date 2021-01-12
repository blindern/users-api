FROM openjdk:11-jre-slim@sha256:3675104c1337a0e5d629c3c34e7e4a603ce137ffbc7c670682dff7c304b959bd

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
