FROM openjdk:11-jre-slim@sha256:c653b8f18b8f3e9e5b801b72eba306c2574a8e3f499be4ac9315caeb3628e7d2

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
