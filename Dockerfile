FROM gradle:6.9.1-jdk17 AS building
ENV JAVA_OPTS="-server -XX:+UseContainerSupport -XX:MinRAMPercentage=5.0 -XX:MaxRAMPercentage=5.0"
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
COPY src ./src
RUN chmod +x gradlew
RUN ./gradlew build

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl libc6-compat bash
ENV TZ=US/Eastern
ENV JAVA_OPTS="-server -XX:+UseContainerSupport -XX:InitialRAMPercentage=50.0 -XX:MaxRAMPercentage=75.0 -javaagent:/opt/opentelemetry-javaagent.jar"
ENV OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4319"
ENV OTEL_SERVICE_NAME="opentelemetry-java"
ENV OTEL_RESOURCE_ATTRIBUTES="deployment.environment=test,service.namespace=example"
# Set environment variables for OpenTelemetry

RUN wget -O /opt/opentelemetry-javaagent.jar https://github.com/grafana/grafana-opentelemetry-java/releases/latest/download/grafana-opentelemetry-java.jar
COPY --from=building /home/gradle/build/libs/opentelemetry-1.0.0.jar /opt/opentelemetry.jar
WORKDIR /opt/
EXPOSE 8080
CMD java $JAVA_OPTS -jar opentelemetry.jar