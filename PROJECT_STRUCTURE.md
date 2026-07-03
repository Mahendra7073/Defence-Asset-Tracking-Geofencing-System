# Project Structure

This document outlines the directory structure of the Defence GIS Tracking System repository.

## Directory Layout

```
Defence-Asset-Tracking-Geofencing-System/
├── .gitignore              # Git ignored files configuration
├── .github/                # GitHub workflows and issue/PR templates
│   ├── ISSUE_TEMPLATE/     # Templates for bug reports and feature requests
│   │   ├── bug_report.md
│   │   └── feature_request.md
│   ├── workflows/
│   │   └── build.yml       # Maven compilation build check
│   └── pull_request_template.md # PR description template
│
├── CONTRIBUTING.md         # Contribution guidelines for developers
├── CODE_OF_CONDUCT.md     # Code of Conduct policy (Contributor Covenant)
├── LICENSE                 # License file (MIT)
├── README.md               # Main repository readme
├── CHANGELOG.md            # Release log and changes list
├── PROJECT_STRUCTURE.md    # Folder structure guide (this file)
├── INSTALLATION.md         # Installation instructions for prerequisites
├── DEPLOYMENT.md           # Tomcat server deployment guide
├── TROUBLESHOOTING.md      # Troubleshooting guide for common issues
├── SECURITY.md             # Security review and best practices
│
├── database/               # PostgreSQL database schema and migrations
│   ├── defence_gis.sql     # Base database schema seed script
│   └── migrations/         # Flyway-compliant SQL migration scripts
│       ├── V002__schema_fixes_and_geofencing.sql
│       ├── V003__bcrypt_passwords_and_seed_data.sql
│       ├── V004__demo_data_expansion.sql
│       └── V005__add_missing_geofence_fields.sql
│
├── docs/                   # System documentation files
│   ├── ARCHITECTURE.md     # Architecture documentation (flows, layers, sequences)
│   ├── API_DOCUMENTATION.md # REST API specifications and response shapes
│   ├── DATABASE.md         # Database schema descriptions, indices, and triggers
│   │
│   ├── images/             # High-quality application interface screenshots
│   │
│   └── database_erd.png    # Database Entity Relationship Diagram
│
├── backend/                # Java Maven backend source project
│   ├── pom.xml             # Maven dependencies configuration
│   └── src/
│       ├── main/
│       │   ├── java/com/drdo/gis/
│       │   │   ├── config/      # HikariCP and database configuration
│       │   │   ├── dao/         # Database Access Objects (DAOs) for SQL queries
│       │   │   ├── filter/      # Authentication Servlet Filters
│       │   │   ├── model/       # Data transfer objects and models
│       │   │   ├── service/     # Business logic (GeofenceService breach detection)
│       │   │   ├── servlet/     # REST Servlets handling HTTP endpoints
│       │   │   └── util/        # Utilities (PasswordUtil BCrypt helper)
│       │   ├── resources/
│       │   │   ├── db.properties        # Active database credentials
│       │   │   └── db.properties.example # Template connection file
│       │   └── webapp/WEB-INF/
│       │       └── web.xml      # Servlet mappings and session configs
│       └── test/                # Unit test classes
│
├── frontend/               # Single Page Application (SPA) frontend static assets
│   ├── index.html          # Redirect controller
│   ├── assets/
│   │   ├── css/            # UI stylesheet modules (variables, layout, components)
│   │   ├── images/         # Static images and Leaflet map markers
│   │   └── js/             # Frontend application logic controllers
│   └── pages/              # HTML content pages
│       ├── alerts.html     # Real-time and historic warnings list
│       ├── assets.html     # Defence fleet log and management
│       ├── dashboard.html  # Stats counters and KPI panels
│       ├── geofence.html   # Geofence zones visual map with view action
│       ├── login.html      # Secure sign-in page
│       ├── reports.html    # Custom alert and tracking logs queries
│       ├── routes.html     # Route history player and WKT loader
│       ├── tracking.html   # Live tracking screen with maps and layers
│       └── users.html      # Active team members table
│
├── geoserver/              # Geoserver SLD maps styling definitions
│   ├── README.md
│   ├── setup_guide.md
│   └── styles/
│       ├── assets.sld      # SLD style for asset points
│       ├── tracks.sld      # SLD style for history path lines
│       └── zones.sld       # SLD style for geofence polygons
│
└── scripts/                # Administrative utilities and simulators
    ├── README.md
    ├── gps_simulator.py    # Python simulator generating mock GPS updates
    ├── setup_database.ps1  # Automated database drop and re-seed script
    └── setup_environment.bat # Setup diagnostics and status checker
```
