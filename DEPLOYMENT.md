# Tomcat Server Deployment Guide

This guide describes how to build, package, and deploy the Defence GIS Tracking System onto an Apache Tomcat application server.

## 1. Prerequisites
- **Java JDK 17** installed and on path.
- **Apache Maven 3.8+** installed.
- **Apache Tomcat 9.0+** downloaded and extracted.
- Environment variable `CATALINA_HOME` set to the Tomcat directory path.

---

## 2. Compilation and Packaging (WAR)
The project is configured as a standard Java web application that compiles down to a Web Archive (WAR) file.

Navigate to the `backend` folder where `pom.xml` is located and run:
```bash
# Clean previous builds and compile + package the project
mvn clean package
```

The output will be built under the `target/` directory:
- Output File: `backend/target/DefenceGIS.war`
- Explosive directory: `backend/target/DefenceGIS/`

---

## 3. Deploying to Tomcat

### Method A: Automated Deployment Script
You can use the deployment setup script in the project root to run database setups, run packaging, and hot-copy the WAR file.

1. Ensure the PostgreSQL database is online.
2. In powershell, run:
```powershell
# Drops and seeds database, compiles code, and copies WAR to webapps
& .\scripts\setup_database.ps1
```

### Method B: Manual Deployment
1. Stop the running Tomcat server instance:
   - On Windows: Run `%CATALINA_HOME%\bin\shutdown.bat`
   - On Linux/macOS: Run `$CATALINA_HOME/bin/shutdown.sh`
2. Delete previous deployments in Tomcat:
   - Remove `%CATALINA_HOME%\webapps\DefenceGIS.war`
   - Remove `%CATALINA_HOME%\webapps\DefenceGIS\` (unpacked directory)
3. Copy the fresh build to Tomcat:
   ```bash
   copy backend\target\DefenceGIS.war %CATALINA_HOME%\webapps\
   ```
4. Start Tomcat:
   - On Windows: Run `%CATALINA_HOME%\bin\startup.bat`
   - On Linux/macOS: Run `$CATALINA_HOME/bin/startup.sh`
5. Tomcat will automatically extract the WAR file and start the web context.

---

## 4. Verifying Deployment
Open your browser and navigate to:
```
http://localhost:8080/DefenceGIS/pages/login.html
```

You can tail the deployment logs to verify the database pools and servlet contexts initialised without warnings:
- Tail Log File: `%CATALINA_HOME%\logs\catalina.out` (or `catalina.log` on Windows)
- HikariCP logs will show:
```
[INFO] HikariCP pool initialized: jdbc:postgresql://localhost:5432/defence_gis
```
