FROM openjdk:11-jre-slim@sha256:915cbc4bc4deea1da0f46fb6d90fbe9db331b9d6cddce4a69f50b477f61d06af

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
