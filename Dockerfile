FROM openjdk:11-jre-slim@sha256:67a7b3533f10903da2b70a9ed3b6a27fed91e01b58046c8f04ab351174a8949d

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
