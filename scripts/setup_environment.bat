@echo off
REM ============================================================
REM Defence GIS - Environment Setup Script (Windows)
REM ============================================================
echo.
echo  ======================================================
echo    Defence Asset Tracking ^& Geofencing System
echo    Environment Setup Checker
echo  ======================================================
echo.

set ERRORS=0

REM --- Java ---
echo [1/6] Checking Java JDK...
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo   [MISSING] Java JDK not found!
    echo   Download: https://adoptium.net/temurin/releases/?version=17
    echo   Install JDK 17+ and add to PATH
    set /A ERRORS+=1
) else (
    java -version 2>&1 | findstr /i "version"
    echo   [OK]
)
echo.

REM --- Maven ---
echo [2/6] Checking Maven...
mvn --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo   [MISSING] Apache Maven not found!
    echo   Download: https://maven.apache.org/download.cgi
    echo   Extract, add bin/ to PATH
    set /A ERRORS+=1
) else (
    mvn --version 2>&1 | findstr /i "Apache Maven"
    echo   [OK]
)
echo.

REM --- PostgreSQL ---
echo [3/6] Checking PostgreSQL...
psql --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo   [MISSING] PostgreSQL not found!
    echo   Download: https://www.postgresql.org/download/windows/
    echo   Install PostgreSQL 15+ with PostGIS bundle
    set /A ERRORS+=1
) else (
    psql --version
    echo   [OK]
)
echo.

REM --- Tomcat ---
echo [4/6] Checking Tomcat...
if defined CATALINA_HOME (
    echo   CATALINA_HOME=%CATALINA_HOME%
    echo   [OK]
) else (
    echo   [MISSING] CATALINA_HOME not set!
    echo   Download: https://tomcat.apache.org/download-90.cgi
    echo   Extract, set CATALINA_HOME environment variable
    set /A ERRORS+=1
)
echo.

REM --- GeoServer ---
echo [5/6] Checking GeoServer...
if exist "C:\Program Files\GeoServer*" (
    echo   [OK] GeoServer found
) else if defined GEOSERVER_HOME (
    echo   GEOSERVER_HOME=%GEOSERVER_HOME%
    echo   [OK]
) else (
    echo   [OPTIONAL] GeoServer not found
    echo   Download: https://geoserver.org/release/stable/
    echo   GeoServer is optional - direct API approach works without it
)
echo.

REM --- Summary ---
echo [6/6] PostGIS Extension Check...
echo   PostGIS is included with PostgreSQL installer
echo   After installing PostgreSQL, run:
echo     CREATE EXTENSION postgis;
echo   inside the defence_gis database.
echo.

echo  ======================================================
if %ERRORS% EQU 0 (
    echo   ALL DEPENDENCIES FOUND
    echo.
    echo   Next steps:
    echo   1. cd database
    echo   2. psql -U postgres -d defence_gis -f defence_gis.sql
    echo   3. psql -U postgres -d defence_gis -f migrations/V002__schema_fixes_and_geofencing.sql
    echo   4. psql -U postgres -d defence_gis -f migrations/V003__bcrypt_passwords_and_seed_data.sql
    echo   5. cd ..\backend
    echo   6. mvn clean package
    echo   7. copy target\DefenceGIS.war %%CATALINA_HOME%%\webapps\
    echo   8. %%CATALINA_HOME%%\bin\startup.bat
    echo   9. Open http://localhost:8080/DefenceGIS/pages/login.html
    echo.
    echo   Login: drdo / drdo2026  OR  admin / admin123  OR  mahendra / mahendra123
) else (
    echo   %ERRORS% MISSING DEPENDENCIES
    echo   Install the missing items above, then re-run this script.
)
echo  ======================================================
pause
