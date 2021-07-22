FROM openjdk:11-jre-slim@sha256:eff0bac8a8556d420fbbcac3be53e3696500fd2ced74ad0529636301221e21f5

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
