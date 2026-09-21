FROM eclipse-temurin:25.0.4_7-jre-alpine@sha256:2ca9adf44f5c29d28ecd26cf92d75cc0c66b7f32bfd839a4439e363a8b428af8

# renovate: datasource=github-releases depName=open-telemetry/opentelemetry-java-instrumentation
ENV OTEL_JAVAAGENT_VERSION=v2.24.0

RUN wget -q "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/${OTEL_JAVAAGENT_VERSION}/opentelemetry-javaagent.jar" \
      -O /opentelemetry-javaagent.jar

ENV JAVA_TOOL_OPTIONS=-javaagent:/opentelemetry-javaagent.jar
ENV OTEL_SERVICE_NAME=users-api
ENV OTEL_EXPORTER_OTLP_ENDPOINT=http://signoz-otel-collector.zt.foreningenbs.no:4317
ENV OTEL_EXPORTER_OTLP_PROTOCOL=grpc
ENV OTEL_METRICS_EXPORTER=otlp
ENV OTEL_TRACES_EXPORTER=otlp
ENV OTEL_LOGS_EXPORTER=otlp

COPY build/libs/users-api-1.0-SNAPSHOT-all.jar /users-api.jar

COPY build/build.properties /build.properties

EXPOSE 8000
CMD ["java", "-jar", "/users-api.jar"]
