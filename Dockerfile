# Build stage
FROM gradle:8.7-jdk17 AS building
WORKDIR /home/gradle
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
COPY src ./src
RUN gradle build --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl libc6-compat bash
ENV TZ=US/Eastern
ENV JAVA_OPTS="-server -XX:+UseContainerSupport -XX:InitialRAMPercentage=50.0 -XX:MaxRAMPercentage=75.0 -javaagent:/opt/opentelemetry-javaagent.jar"
ENV OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4319"
ENV OTEL_SERVICE_NAME="opentelemetry-java"
ENV OTEL_RESOURCE_ATTRIBUTES="deployment.environment=test,service.namespace=example"
# Set environment variables for OpenTelemetry

# Copy the built JAR from the building stage
COPY --from=building /home/gradle/build/libs/opentelemetry-1.0.0.jar /opt/app.jar

# Download OpenTelemetry agent
RUN wget -O /opt/opentelemetry-javaagent.jar \
    https://github.com/grafana/grafana-opentelemetry-java/releases/latest/download/grafana-opentelemetry-java.jar

WORKDIR /opt/
EXPOSE 8080

CMD ["sh", "-c", "java $JAVA_OPTS -jar /opt/app.jar"]
