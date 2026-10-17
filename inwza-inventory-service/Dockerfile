# Build from ecosystem root:
#   docker build -f inwza-inventory-service/Dockerfile -t inwza-inventory-service:local .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY inwza-api-contracts ./inwza-api-contracts
RUN mvn -f inwza-api-contracts -q install -DskipTests
COPY inwza-inventory-service ./inwza-inventory-service
RUN mvn -f inwza-inventory-service -q package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache wget \
    && addgroup -S inwza && adduser -S inwza -G inwza
USER inwza
COPY --from=build /workspace/inwza-inventory-service/target/inwza-inventory-service-0.1.0-SNAPSHOT-boot.jar /app/app.jar
EXPOSE 8084 50054
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://127.0.0.1:${SERVER_PORT:-8084}/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
