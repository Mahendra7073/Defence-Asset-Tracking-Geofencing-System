# Security Review

This document lists the security policies, implementations, and reviews incorporated inside the Defence GIS Tracking System.

## Security Controls

### 1. Authentication
- User passwords are secure and hashed in the database using the **BCrypt** algorithm (strength cost factor `12`).
- Plaintext passwords are never stored in the database.
- Login authentication compares hashed strings using secure salt comparisons (`BCrypt.checkpw`) to prevent timing attacks.

### 2. Session Management
- Successful authentication initializes an HTTP session via Java Servlets (`req.getSession(true)`).
- Session variables (`userId`, `username`, `role`) are bound to the server context.
- Sessions automatically timeout after **30 minutes** of inactivity (`session.setMaxInactiveInterval(30 * 60)`).
- Logout invalidates the active HTTP session, clearing cookies and forcing redirection to the login portal.

### 3. Parameterized SQL Queries
- To prevent **SQL Injection (SQLi)** vulnerabilities, the DAO layers query database records strictly using `PreparedStatement` placeholders (`?`).
- Parameter values are bound dynamically using type-specific setters (e.g. `ps.setInt`, `ps.setString`).
- Raw string concatenation in SQL execution is strictly forbidden.

### 4. Separation of Secrets
- Database passwords and configurations are stored in `backend/src/main/resources/db.properties`.
- This file is ignored by Git via the `.gitignore` rule `*.properties`.
- A template template file (`db.properties.example`) is supplied instead to ensure database credentials are never committed to the public repository.

### 5. API Endpoint Authorization
- The Java filter `AuthFilter.java` intercepting all `/api/*` requests verifies session validity before delegating requests.
- Anonymous requests are rejected immediately with a `401 Unauthorized` response.
- Exclusions are restricted to authentication endpoints (`/api/auth/login`, `/api/auth/session`) and CORS preflight OPTIONS queries.
