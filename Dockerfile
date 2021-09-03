FROM openjdk:11-jre-slim@sha256:0d779d441c98b786a3aac943acf6a9a89721834204217aac17930d3b79561c84

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
