# Build from ecosystem root:
#   docker build -f inwza-item-service/Dockerfile -t inwza-item-service:local .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY inwza-api-contracts ./inwza-api-contracts
RUN mvn -f inwza-api-contracts -q install -DskipTests
COPY inwza-item-service ./inwza-item-service
RUN mvn -f inwza-item-service -q package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache wget \
    && addgroup -S inwza && adduser -S inwza -G inwza
USER inwza
COPY --from=build /workspace/inwza-item-service/target/inwza-item-service-0.1.0-SNAPSHOT-boot.jar /app/app.jar
EXPOSE 8082 50052
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://127.0.0.1:${SERVER_PORT:-8082}/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
