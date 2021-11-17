FROM openjdk:11-jre-slim@sha256:fb760b3216c7bac47a131ec031d8864c82b9de12717a96d32b874d2e59132576

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
