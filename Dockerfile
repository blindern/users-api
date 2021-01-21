FROM openjdk:11-jre-slim@sha256:f7113ee8f8f729fb513621957ce7d16784faeb576f64f89e828b35743df2efbe

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
