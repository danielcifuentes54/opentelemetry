FROM gradle:6.9.1-jdk17 AS building
ENV JAVA_OPTS="-server -XX:+UseContainerSupport -XX:MinRAMPercentage=5.0 -XX:MaxRAMPercentage=5.0"
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
COPY src ./src
RUN chmod +x gradlew
RUN ./gradlew build

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl
ENV TZ=US/Eastern
ENV JAVA_OPTS="-server -XX:+UseContainerSupport -XX:InitialRAMPercentage=50.0 -XX:MaxRAMPercentage=75.0"
COPY --from=building /home/gradle/build/libs/e-commerce-inventory-service-0.0.1-SNAPSHOT.jar /opt/e-commerce-inventory-service.jar
WORKDIR /opt/
EXPOSE 8080
CMD java $JAVA_OPTS -jar e-commerce-inventory-service.jar
