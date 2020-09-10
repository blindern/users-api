FROM openjdk:11-jre-slim@sha256:c63aea030d5933a0de02545a249083216d14807eb6110d83f10673b8588da375

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
