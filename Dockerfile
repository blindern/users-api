FROM eclipse-temurin:17-jre-alpine@sha256:7171081bdd19d577be0ec4633e14888358cfa24e336c4aa068b81a1cc6fd19b1

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

COPY build/build.properties /build.properties

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
