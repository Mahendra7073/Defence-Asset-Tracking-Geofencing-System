# REST API Documentation

This document describes the API endpoints exposed by the Defence GIS Tracking System.

## Base URL
All API calls are relative to:
```
http://localhost:8080/DefenceGIS/api
```

## Authentication & Authorization
All endpoints under `/api/*` (except `/api/auth/login` and `/api/auth/session`) require an active HTTP Session. If unauthorized, the server responds with:
- **Status:** `401 Unauthorized`
- **JSON:**
```json
{
    "status": "error",
    "code": 401,
    "message": "Unauthorized — please log in"
}
```

---

## 1. Authentication API

### POST `/auth/login`
Authenticate user and create a session.

* **Headers:** `Content-Type: application/json`
* **Body:**
```json
{
    "username": "admin",
    "password": "admin123"
}
```
* **Response (200 OK):**
```json
{
    "status": "success",
    "message": "Login successful",
    "data": {
        "userId": 1,
        "username": "admin",
        "fullName": "System Administrator",
        "role": "ADMIN"
    }
}
```

---

### POST `/auth/logout`
Invalidate current session and sign out.

* **Response (200 OK):**
```json
{
    "status": "success",
    "message": "Logged out"
}
```

---

### GET `/auth/session`
Check current active session state.

* **Response (200 OK):**
```json
{
    "status": "success",
    "message": "Session active",
    "data": {
        "userId": 1,
        "username": "admin",
        "role": "ADMIN",
        "isActive": true
    }
}
```

---

## 2. Dashboard API

### GET `/dashboard`
Fetch system-wide counters and alerts summary.

* **Response (200 OK):**
```json
{
    "status": "success",
    "data": {
        "totalAssets": 15,
        "activeAssets": 15,
        "totalZones": 6,
        "unacknowledgedAlerts": 12
    }
}
```

---

## 3. Assets API

### GET `/assets`
Fetch list of registered assets.

* **Query Parameters:**
  * `type` (optional): Filter assets by type (e.g. `vehicle`, `drone`, `person`, `tank`).
* **Response (200 OK):**
```json
{
    "status": "success",
    "data": [
        {
            "id": 1,
            "assetName": "Vehicle-01",
            "assetType": "vehicle",
            "assetCode": "VH-001",
            "description": "Armoured patrol vehicle",
            "status": "active",
            "createdAt": "2026-06-22 10:15:30"
        }
    ]
}
```

---

### GET `/assets/{id}`
Fetch detailed attributes of a single asset.

* **Response (200 OK):**
```json
{
    "status": "success",
    "data": {
        "id": 1,
        "assetName": "Vehicle-01",
        "assetType": "vehicle",
        "assetCode": "VH-001",
        "description": "Armoured patrol vehicle",
        "status": "active",
        "createdAt": "2026-06-22 10:15:30"
    }
}
```

---

### POST `/assets`
Create a new defence asset.

* **Headers:** `Content-Type: application/json`
* **Body:**
```json
{
    "assetName": "Drone-05",
    "assetType": "drone",
    "assetCode": "DR-005",
    "description": "Border recon copter",
    "status": "active"
}
```
* **Response (210 Created):**
```json
{
    "status": "success",
    "message": "Asset created",
    "data": {
        "id": 16
    }
}
```

---

### PUT `/assets/{id}`
Update existing asset attributes.

* **Headers:** `Content-Type: application/json`
* **Body:**
```json
{
    "assetName": "Drone-05-Mod",
    "assetType": "drone",
    "assetCode": "DR-005",
    "description": "Border recon copter modified",
    "status": "active"
}
```
* **Response (200 OK):**
```json
{
    "status": "success",
    "message": "Asset updated"
}
```

---

### DELETE `/assets/{id}`
Delete a defence asset.

* **Response (200 OK):**
```json
{
    "status": "success",
    "message": "Asset deleted"
}
```

---

## 4. Positions / GPS Ingestion API

### POST `/positions`
Ingest GPS coordinates from field assets and calculate geofence breaches.

* **Headers:** `Content-Type: application/json`
* **Body:**
```json
{
    "assetId": 1,
    "longitude": 73.0243,
    "latitude": 26.2389,
    "speed": 45.5,
    "heading": 90.0,
    "altitude": 250.0,
    "accuracy": 3.2
}
```
* **Response (201 Created):**
```json
{
    "status": "success",
    "data": {
        "positionId": 1420,
        "alerts": [
            {
                "id": 482,
                "assetId": 1,
                "zoneId": 3,
                "alertType": "ENTER",
                "severity": "HIGH",
                "lat": 26.2389,
                "lng": 73.0243,
                "acknowledged": false,
                "triggeredAt": "2026-06-30 15:40:02",
                "assetName": "Vehicle-01",
                "zoneName": "Zone-Charlie-Command-Warning"
            }
        ]
    }
}
```

---

### GET `/positions/latest`
Fetch latest coordinates for all active assets. Returns GeoJSON FeatureCollection.

* **Response (200 OK):**
```json
{
    "status": "success",
    "data": {
        "type": "FeatureCollection",
        "features": [
            {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [73.0243, 26.2389]
                },
                "properties": {
                    "assetId": 1,
                    "assetName": "Vehicle-01",
                    "assetType": "vehicle",
                    "assetCode": "VH-001",
                    "speed": 45.5,
                    "heading": 90.0,
                    "altitude": 250.0,
                    "recordedAt": "2026-06-30 15:40:02"
                }
            }
        ]
    }
}
```

---

## 5. Geofences API

### GET `/geofences`
Fetch all active geofence zones. Returns GeoJSON FeatureCollection.

* **Response (200 OK):**
```json
{
    "status": "success",
    "data": {
        "type": "FeatureCollection",
        "features": [
            {
                "type": "Feature",
                "geometry": {
                    "type": "Polygon",
                    "coordinates": [[[73.01, 26.23], [73.05, 26.23], [73.05, 26.26], [73.01, 26.26], [73.01, 26.23]]]
                },
                "properties": {
                    "id": 1,
                    "zoneName": "Zone-Alpha-Restricted",
                    "zoneType": "restricted",
                    "color": "#FF0000",
                    "isActive": true
                }
            }
        ]
    }
}
```

---

### GET `/geofences/{id}`
Fetch rich details and statistics for a specific geofence zone.

* **Response (200 OK):**
```json
{
    "status": "success",
    "data": {
        "id": 1,
        "zoneName": "Zone-Alpha-Restricted",
        "zoneType": "restricted",
        "status": "active",
        "createdAt": "2026-06-22 10:20:00",
        "coordinates": "POLYGON((73.01 26.23, 73.05 26.23, 73.05 26.26, 73.01 26.26, 73.01 26.23))",
        "area": 12450832.22,
        "assetsInsideCount": 2,
        "alertsCount": 4
    }
}
```

---

## 6. Alerts API

### GET `/alerts`
Fetch warnings logs.

* **Query Parameters:**
  * `unack` (optional): If `true`, returns only unacknowledged warnings.
  * `severity` (optional): Filter by severity (e.g. `HIGH`, `MEDIUM`, `LOW`).
  * `limit` (optional): Result limit (default: `50`).
* **Response (200 OK):**
```json
{
    "status": "success",
    "data": [
        {
            "id": 482,
            "assetId": 1,
            "zoneId": 3,
            "alertType": "ENTER",
            "severity": "HIGH",
            "lat": 26.2389,
            "lng": 73.0243,
            "acknowledged": false,
            "ackBy": 0,
            "triggeredAt": "2026-06-30 15:40:02",
            "assetName": "Vehicle-01",
            "zoneName": "Zone-Charlie-Command-Warning"
        }
    ]
}
```

---

### PUT `/alerts/{id}/acknowledge`
Acknowledge a specific alert.

* **Response (200 OK):**
```json
{
    "status": "success",
    "message": "Alert acknowledged"
}
```

---

### PUT `/alerts/acknowledge-all`
Acknowledge all unacknowledged alerts.

* **Response (200 OK):**
```json
{
    "status": "success",
    "message": "Alerts acknowledged: 12"
}
```

---

## 7. Track History API

### GET `/tracks`
Fetch historical breadcrumb route path of an asset. Returns GeoJSON Feature.

* **Query Parameters:**
  * `assetId` (required): ID of the asset.
  * `start` (optional): Date (YYYY-MM-DD).
  * `end` (optional): Date (YYYY-MM-DD).
* **Response (200 OK):**
```json
{
    "status": "success",
    "data": {
        "type": "Feature",
        "geometry": {
            "type": "LineString",
            "coordinates": [[73.0243, 26.2389], [73.0250, 26.2395], [73.0260, 26.2402]]
        },
        "properties": {
            "assetId": 1,
            "pointCount": 3,
            "distanceM": 210.45,
            "avgSpeed": 47.66,
            "startedAt": "2026-06-29 09:00:00",
            "endedAt": "2026-06-29 09:05:00"
        }
    }
}
```

---

## 8. Users API

### GET `/users`
Fetch registered team members.

* **Response (200 OK):**
```json
{
    "status": "success",
    "data": [
        {
            "id": 1,
            "username": "admin",
            "fullName": "System Administrator",
            "role": "ADMIN",
            "email": "admin@drdo.gov.in",
            "active": true,
            "lastLogin": "2026-06-30 15:00:00",
            "createdAt": "2026-06-22 10:00:00"
        }
    ]
}
```
