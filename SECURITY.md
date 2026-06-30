# Security Policy - Defence GIS Tracking System

This document outlines the security controls, authentication mechanisms, data protection patterns, and recommendations implemented inside the Defence GIS Tracking System.

## Supported Versions

Only the latest release is supported with security patches:

| Version | Supported |
| --- | --- |
| **1.1.x** | Yes ✅ |
| **1.0.x** | No ❌ |

---

## Security Implementation

### 1. Cryptographic Authentication & Password Hashing
- User passwords are secure and hashed in the database using the **BCrypt** adaptive hashing algorithm.
- Cost Factor parameter: `12` (provides strong resistance against brute-force attacks while preserving server performance).
- Salt generation is handled internally by BCrypt during password creation.
- Plaintext passwords are never logged, compared directly, or stored.
- Salt comparisons during authentication use the secure `BCrypt.checkpw` constant-time matching function to prevent timing attacks.

### 2. Session Management
- Successful user authentication initializes an HTTP session via Java Servlets (`req.getSession(true)`).
- Critical metadata attributes (`userId`, `username`, `role`) are stored server-side.
- Inactive operator sessions automatically invalidate after **30 minutes** (configured via `session.setMaxInactiveInterval(30 * 60)`).
- Session terminations (calling `/api/auth/logout`) explicitly invoke `session.invalidate()` to purge cookies and server-side contexts, redirecting the client immediately to the login interface.

### 3. SQL Injection (SQLi) Protection
- Raw SQL string concatenation is forbidden.
- The Data Access Object (DAO) tier queries PostgreSQL database tables using `PreparedStatement` parameters (`?`).
- Variable bindings are set dynamically using strongly-typed APIs:
  - `ps.setInt(...)`
  - `ps.setString(...)`
  - `ps.setDouble(...)`
  - `ps.setTimestamp(...)`
- Inputs are automatically escaped by PostgreSQL drivers during binding.

### 4. Separation of Secrets & Environments
- Database coordinates, credentials, and HikariCP pool parameters are isolated inside the local file:
  `backend/src/main/resources/db.properties`.
- This file is ignored by Git using the global pattern: `*.properties`.
- The repository provides a template template `db.properties.example` for secure deployment.

### 5. Input Validation
- Servlets inspect request body JSON schemas before updating records.
- Validation checks verify parameters:
  - Asset IDs must be positive non-zero integers.
  - Coordinate ranges must fall within valid lat/lng parameters: `-180.0 <= longitude <= 180.0` and `-90.0 <= latitude <= 90.0`.
  - Empty or null values are rejected with standard HTTP status code `400 Bad Request`.

---

## Future Security Recommendations
- **Transport Layer Security (TLS):** Enforce HTTPS on all connections using Tomcat SSL certificates to prevent man-in-the-middle sniffing of session IDs.
- **Cross-Site Request Forgery (CSRF):** Integrate anti-CSRF token parameters on all state-modifying requests (POST/PUT/DELETE).
- **Web Application Firewall (WAF):** Place the Tomcat instance behind a reverse proxy (e.g. Nginx or Cloudflare) configured to rate-limit coordinates ingestion endpoints.
