# Contracts are a Maven library, not a runtime image. Use CI to publish the artifact.
# This Dockerfile validates the proto build only (optional smoke image).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package
