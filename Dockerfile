# ============================================================
# Defence GIS Tracking System — Multi-Stage Dockerfile
# Stage 1: Maven build   (compile + package WAR)
# Stage 2: Tomcat deploy  (copy WAR into Tomcat 9)
# ============================================================

# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy Maven POM first for dependency caching
COPY backend/pom.xml ./backend/pom.xml
RUN cd backend && mvn dependency:go-offline -B

# Copy source code and frontend
COPY backend/src ./backend/src
COPY frontend ./frontend

# Override db.properties with Docker-specific version
COPY docker/tomcat/db.properties ./backend/src/main/resources/db.properties

# Build WAR (skip tests for faster builds)
RUN cd backend && mvn clean package -DskipTests -B

# ---- Stage 2: Runtime ----
FROM tomcat:9.0-jre17-temurin

# Remove default Tomcat webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the built WAR from builder stage
COPY --from=builder /build/backend/target/DefenceGIS.war /usr/local/tomcat/webapps/DefenceGIS.war

# Expose Tomcat HTTP port
EXPOSE 8080

# Health check: verify Tomcat is serving the app
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
    CMD curl -f http://localhost:8080/DefenceGIS/ || exit 1

# Start Tomcat
CMD ["catalina.sh", "run"]
