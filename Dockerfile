FROM openjdk:11-jre-slim@sha256:8a162fc28d509cc96aa011c3cdc7a2fa4c684f1e4af72b84d169057eec30411c

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
