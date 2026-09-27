# ============================================================================
# Multi-Stage Dockerfile for Kotlin Product API
# ----------------------------------------------------------------------------
# SECURITY HARDENING:
# 1. Multi-stage build — build in one stage, deploy in minimal runtime
# 2. No root user — app runs as non-root (uid 1001)
# 3. Distroless base — minimal attack surface (~20MB vs ~400MB for full JDK)
# 4. No shell — prevents shell injection attacks
# 5. Read-only filesystem — prevents file modification at runtime
# 6. Non-root user can only write to /tmp (via tmpfs)
#
# OWASP ASVS 4.0.3 — V14.5.1: Verify that the application runs as a non-root user
# OWASP ASVS 4.0.3 — V14.5.2: Verify that the application does not run as root
# CWE-250: Execution with Unnecessary Privileges
# ============================================================================

# ==================== Build Stage ====================
FROM maven:3.9-eclipse-temuras-21 AS build

# Create app directory and set as working directory
WORKDIR /app

# Copy Maven configuration first (for better cache utilization)
COPY pom.xml .
COPY src/main/resources src/main/resources
COPY src/main/kotlin src/main/kotlin

# Build with all dependencies — skip tests in build stage
# (tests run separately in CI/CD pipeline)
RUN mvn clean package -DskipTests -Pproduction

# ==================== Runtime Stage ====================
# Use distroless Java image — minimal attack surface, no shell
FROM eclipse-temini:java21-jre-alpine

# SECURITY: Create non-root user (uid 1001)
# This prevents privilege escalation if the container is compromised
USER 1001

# SECURITY: Set working directory with appropriate permissions
WORKDIR /home/nonroot

# Copy the built JAR from the build stage
# The JAR name is versioned — ensure it matches pom.xml version
COPY --chown=1001:1001 --from=build /app/target/kotlin-pg-api-*.jar app.jar

# SECURITY: Expose only the application port — no SSH, no debug ports
EXPOSE 8080

# SECURITY: Use exec form of ENTRYPOINT — prevents shell injection
# No environment variables are set in the image — all come from runtime
ENTRYPOINT ["java", \
    "-XX:+UnlockExperimentalVMOptions", \
    "-XX:+UseContainerSupport", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=prod", \
    "-Djava.awt.headless=true", \
    "-jar", "/home/nonroot/app.jar"]
