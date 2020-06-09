FROM openjdk:11-jre-slim@sha256:1fb56466022f61c64b1fb5f15450619626fd4dd4c63d81c29f1142120df374cf

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
