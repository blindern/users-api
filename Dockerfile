FROM openjdk:11-jre-slim@sha256:4920839af4a94b1e4f502b874153ef6cb8330ed20160e30233d4e88ff5ff2060

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
