FROM openjdk:11-jre-slim@sha256:46285dcf00260440e00dcb68b5432cbed8f5fea734470be6ffb8ac7cc052024b

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
