FROM openjdk:11-jre-slim@sha256:0121054e9a145186cf0da6fbde109311ca4f709250059f837c26a0d869d8f261

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
