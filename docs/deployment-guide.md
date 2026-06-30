# Deployment Guide — Defence GIS Tracking System

## Prerequisites

| Software       | Version    | Purpose                    |
| -------------- | ---------- | -------------------------- |
| Java JDK       | 17+        | Backend compilation        |
| Apache Maven   | 3.9+       | Build tool                 |
| Apache Tomcat  | 9.x/10.x  | Servlet container          |
| PostgreSQL     | 18         | Database server            |
| PostGIS        | 3.6        | Spatial extension          |
| GeoServer      | 2.25.x    | WMS/WFS GIS server         |

---

## Step 1: Database Setup

```bash
# 1. Create database
psql -U postgres -c "CREATE DATABASE defence_gis;"

# 2. Enable PostGIS
psql -U postgres -d defence_gis -c "CREATE EXTENSION IF NOT EXISTS postgis;"

# 3. Load schema
psql -U postgres -d defence_gis -f database/defence_gis.sql

# 4. Verify
psql -U postgres -d defence_gis -c "SELECT PostGIS_Version();"
```

---

## Step 2: Configure Database Connection

```bash
# Copy example properties
cp backend/src/main/resources/db.properties.example \
   backend/src/main/resources/db.properties

# Edit with your PostgreSQL credentials
# db.url=jdbc:postgresql://localhost:5432/defence_gis
# db.username=postgres
# db.password=YOUR_PASSWORD
```

---

## Step 3: Build Backend

```bash
cd backend
mvn clean package
# Output: backend/target/DefenceGIS.war
```

---

## Step 4: Deploy to Tomcat

```bash
# Copy WAR to Tomcat webapps
cp backend/target/DefenceGIS.war $CATALINA_HOME/webapps/

# Start Tomcat
$CATALINA_HOME/bin/startup.sh

# Verify
curl http://localhost:8080/DefenceGIS/
```

---

## Step 5: Configure GeoServer

Follow instructions in: `geoserver/setup_guide.md`

1. Create workspace: `defence`
2. Create PostGIS store: `defence_gis`
3. Publish layers: `asset_positions`, `geofence_zones`, `track_history`
4. Apply SLD styles from `geoserver/styles/`

---

## Step 6: Verify Complete System

| Component   | URL                                              | Expected           |
| ----------- | ------------------------------------------------ | ------------------- |
| Tomcat      | `http://localhost:8080/DefenceGIS/`               | Landing page        |
| Login       | `http://localhost:8080/DefenceGIS/pages/login.html` | Login form        |
| API Health  | `http://localhost:8080/DefenceGIS/api/dashboard`   | JSON stats         |
| GeoServer   | `http://localhost:8080/geoserver/web/`              | GeoServer admin    |
| WMS Preview | `http://localhost:8080/geoserver/defence/wms?...`   | Map tiles          |

---

## Common Issues

| Issue                                   | Solution                                       |
| --------------------------------------- | ---------------------------------------------- |
| `ClassNotFoundException: PostGIS`       | Ensure postgis-jdbc is in pom.xml              |
| `Connection refused: 5432`              | Check PostgreSQL service is running            |
| `404 on /api/*`                         | Uncomment servlet mappings in web.xml          |
| GeoServer layer error                   | Verify PostGIS store connection parameters     |
| Leaflet tiles not loading               | Check internet connectivity for OSM tiles      |
