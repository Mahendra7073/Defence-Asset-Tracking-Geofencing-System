# Docker & Containerization Guide
## Defence Asset Tracking & Geofencing System

This guide explains how to build, run, configure, and manage the containerized environment for the Defence GIS Tracking System.

---

## 🏗️ Decoupled Architecture

The containerized stack consists of three services managed by `docker-compose.yml`:

1. **`db` (PostgreSQL 15 + PostGIS 3):** Relational & spatial database storage. Exposes port `5432` locally.
2. **`tomcat` (Apache Tomcat 9.0 + Temurin JDK 17):** Multi-stage Java EE application runtime container. Exposes port `8080` locally.
3. **`geoserver` (Optional GIS Map Server):** Exposes port `8082` for rendering complex spatial maps.

---

## 🚀 One-Command Deployment

To pull images, compile the Java source project, build custom images, and spin up all services:

```bash
docker compose up -d
```

### Build & Package Internals (Multi-Stage Dockerfile)
The `Dockerfile` performs compilation and packaging completely inside the builder image, preventing the need for local Java or Maven installations:

```dockerfile
# Stage 1: Build Java WAR artifact using Maven
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY backend/pom.xml ./backend/pom.xml
RUN cd backend && mvn dependency:go-offline -B
COPY backend/src ./backend/src
COPY frontend ./frontend
COPY docker/tomcat/db.properties ./backend/src/main/resources/db.properties
RUN cd backend && mvn clean package -DskipTests -B

# Stage 2: Serve WAR artifact using Tomcat
FROM tomcat:9.0-jre17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=builder /build/backend/target/DefenceGIS.war /usr/local/tomcat/webapps/DefenceGIS.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
```

---

## 🔧 Useful Administration Commands

### 1. View Logs
View real-time logs from the Tomcat servlet context and PostgreSQL server:
```bash
# Follow all container logs
docker compose logs -f

# Follow Tomcat specific logs
docker compose logs -f tomcat
```

### 2. Verify Database Connection
Connect directly to the PostgreSQL console inside the database container:
```bash
docker exec -it defence-postgres psql -U postgres -d defence_gis
```

### 3. Force Rebuild After Code Changes
When modifying CSS, JS, HTML, or Java files in the workspace, you must rebuild the Tomcat image to compile the changes:
```bash
# Build the tomcat service without cache
docker compose build tomcat

# Re-create and restart the tomcat container
docker compose up -d tomcat
```

### 4. Tear Down Environment
Stop and remove all containers and shared network resources:
```bash
docker compose down
```

To remove all persistent database volumes as well (Full Hard Reset):
```bash
docker compose down -v
```
