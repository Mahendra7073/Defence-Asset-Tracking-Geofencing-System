# Installation Guide

This document provides step-by-step instructions to install and configure all prerequisites for the Defence GIS Tracking System.

## 1. System Requirements
- **Operating System:** Windows 10/11, Ubuntu 20.04+, or macOS.
- **Java:** JDK 17 LTS (e.g. Temurin or Oracle JDK).
- **Database:** PostgreSQL 15+ with PostGIS 3+ bundle extension.
- **Server:** Apache Tomcat 9.0.x (Java EE 8 compatible).
- **Build Tool:** Apache Maven 3.8+.

---

## 2. JDK 17 Installation
1. Download Java JDK 17 installer from [Adoptium (Temurin)](https://adoptium.net/temurin/releases/?version=17).
2. Run the installer, checking the options to set `JAVA_HOME` and add java bin directory to the PATH.
3. Open a terminal and verify:
```bash
java -version
```

---

## 3. PostgreSQL & PostGIS Installation

### Windows
1. Download the PostgreSQL installer from [PostgreSQL Download page](https://www.postgresql.org/download/windows/).
2. Run the installer and complete the wizard. Note down the superuser password (default user: `postgres`).
3. At the end of the installation, launch **Stack Builder**.
4. In Stack Builder, select your PostgreSQL installation, navigate to **Spatial Extensions**, check the **PostGIS** bundle, and install it.

### Linux (Ubuntu)
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib postgis postgresql-15-postgis-3
```

---

## 4. Apache Tomcat Installation
1. Download the core zip/tar.gz package for Apache Tomcat 9.0.x from [Tomcat 9 Downloads page](https://tomcat.apache.org/download-90.cgi).
2. Extract the archive into a folder of your choice (e.g. `C:\tomcat` or `/opt/tomcat`).
3. Set the environment variable `CATALINA_HOME` to point to the extracted root folder.
4. Verify by running the startup script:
   - Windows: `%CATALINA_HOME%\bin\startup.bat`
   - Linux: `sh $CATALINA_HOME/bin/startup.sh`
5. Visit `http://localhost:8080` to verify Tomcat welcome page.

---

## 5. Apache Maven Installation
1. Download the binary zip file for Apache Maven from [Maven Downloads page](https://maven.apache.org/download.cgi).
2. Extract the archive into a folder.
3. Add the `bin/` directory of the extracted maven path to your system's `PATH` variable.
4. Verify:
```bash
mvn -version
```

---

## 6. Configuring Database Properties
Before building the backend, customize your database password:

1. Copy the example properties file:
   ```bash
   cp backend/src/main/resources/db.properties.example backend/src/main/resources/db.properties
   ```
2. Open `backend/src/main/resources/db.properties` and edit the password field:
   ```properties
   db.url=jdbc:postgresql://localhost:5432/defence_gis
   db.username=postgres
   db.password=YOUR_POSTGRESQL_PASSWORD
   ```
3. Save the file.
