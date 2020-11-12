FROM openjdk:11-jre-slim@sha256:107725da9b2e3272a5676e318a2f61474450d93c7548458437175d5fcf6b7a7c

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
