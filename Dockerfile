FROM openjdk:11-jre-slim@sha256:34744515bdde22d92a627555165e72b4e3e7aca2094cc025cb0f4000058a3c1c

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
