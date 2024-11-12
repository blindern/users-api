FROM eclipse-temurin:21.0.5_11-jre-alpine@sha256:8a929133a9720468599ca8f854eea6514435e050606dbbb4cd17478c22e9fe37

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

COPY build/build.properties /build.properties

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
