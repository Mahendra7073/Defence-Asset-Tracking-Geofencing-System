# API Specification — Defence GIS Tracking System

## Base URL

```
http://localhost:8080/DefenceGIS/api
```

## Response Format

All API responses use JSON format:

```json
{
    "status": "success" | "error",
    "data": { ... },
    "message": "Human-readable message"
}
```

## Error Response

```json
{
    "status": "error",
    "code": 401,
    "message": "Unauthorized — session expired"
}
```

---

## 1. Authentication API

### POST `/api/auth/login`
Authenticate user and create session.

**Request:**
```json
{
    "username": "admin",
    "password": "admin123"
}
```

**Success Response (200):**
```json
{
    "status": "success",
    "data": {
        "userId": 1,
        "username": "admin",
        "fullName": "System Administrator",
        "role": "ADMIN"
    }
}
```

**Error Response (401):**
```json
{
    "status": "error",
    "message": "Invalid username or password"
}
```

---

### POST `/api/auth/logout`
Invalidate current session.

**Response (200):**
```json
{
    "status": "success",
    "message": "Logged out"
}
```

---

### GET `/api/auth/session`
Check if current session is valid.

**Response (200):**
```json
{
    "status": "success",
    "data": {
        "userId": 1,
        "role": "admin",
        "isActive": true
    }
}
```

---

## 2. Assets API

### GET `/api/assets`
List all assets.

**Query Parameters:**
| Parameter | Type   | Description                     |
| --------- | ------ | ------------------------------- |
| `status`  | string | Filter: "active" or "inactive"  |
| `type`    | string | Filter: "vehicle", "drone", etc.|

**Response (200):**
```json
{
    "status": "success",
    "data": [
        {
            "id": 1,
            "assetName": "Vehicle-01",
            "assetType": "vehicle",
            "assetCode": "VH-001",
            "status": "active"
        }
    ]
}
```

---

### POST `/api/assets`
Create a new asset.

**Request:**
```json
{
    "assetName": "Drone-02",
    "assetType": "drone",
    "assetCode": "DR-002",
    "description": "Surveillance drone"
}
```

**Response (201):**
```json
{
    "status": "success",
    "data": { "id": 5 },
    "message": "Asset created"
}
```

---

### PUT `/api/assets/{id}`
Update an asset.

### DELETE `/api/assets/{id}`
Delete an asset.

---

## 3. Positions API

### POST `/api/positions`
Ingest a new GPS position. Triggers geofence breach detection.

**Request:**
```json
{
    "assetId": 1,
    "latitude": 26.2389,
    "longitude": 73.0243,
    "speed": 45.0,
    "heading": 90.0,
    "altitude": 320.0,
    "accuracy": 5.0
}
```

**Response (201):**
```json
{
    "status": "success",
    "data": {
        "positionId": 42,
        "alerts": [
            {
                "alertId": 10,
                "zoneName": "Zone-Alpha-Restricted",
                "alertType": "ENTER",
                "severity": "HIGH"
            }
        ]
    }
}
```

---

### GET `/api/positions/latest`
Get latest position for each active asset (for map rendering).

**Response (200):**
```json
{
    "status": "success",
    "data": {
        "type": "FeatureCollection",
        "features": [
            {
                "type": "Feature",
                "geometry": { "type": "Point", "coordinates": [73.0243, 26.2389] },
                "properties": {
                    "assetId": 1,
                    "assetName": "Vehicle-01",
                    "assetType": "vehicle",
                    "speed": 45.0,
                    "heading": 90.0,
                    "recordedAt": "2026-06-22T00:15:00Z"
                }
            }
        ]
    }
}
```

---

## 4. Geofences API

### GET `/api/geofences`
List all geofence zones as GeoJSON.

**Response (200):**
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

### POST `/api/geofences`
Create a new geofence zone.

**Request:**
```json
{
    "zoneName": "Zone-Charlie-Warning",
    "zoneType": "warning",
    "color": "#FFA500",
    "coordinates": [[73.01, 26.23], [73.05, 26.23], [73.05, 26.26], [73.01, 26.26], [73.01, 26.23]]
}
```

---

## 5. Alerts API

### GET `/api/alerts`
List alerts.

**Query Parameters:**
| Parameter | Type    | Description                      |
| --------- | ------- | -------------------------------- |
| `unack`   | boolean | If true, only unacknowledged     |
| `assetId` | int     | Filter by asset ID               |
| `limit`   | int     | Max results (default 50)         |

---

### PUT `/api/alerts/{id}/acknowledge`
Acknowledge an alert.

**Response (200):**
```json
{
    "status": "success",
    "message": "Alert acknowledged"
}
```

---

## 6. Dashboard API

### GET `/api/dashboard`
Aggregated statistics.

**Response (200):**
```json
{
    "status": "success",
    "data": {
        "totalAssets": 4,
        "activeAssets": 3,
        "totalZones": 2,
        "unacknowledgedAlerts": 1,
        "todayAlertCount": 3,
        "recentAlerts": [ ... ]
    }
}
```

---

## 7. Tracks API

### GET `/api/tracks?assetId={id}&start={ISO}&end={ISO}`
Get route history for an asset as GeoJSON LineString.

**Response (200):**
```json
{
    "status": "success",
    "data": {
        "type": "Feature",
        "geometry": {
            "type": "LineString",
            "coordinates": [[73.02, 26.23], [73.03, 26.24], [73.04, 26.25]]
        },
        "properties": {
            "assetId": 1,
            "distanceM": 2450.5,
            "avgSpeed": 38.2,
            "pointCount": 47,
            "startedAt": "2026-06-21T08:00:00Z",
            "endedAt": "2026-06-21T09:30:00Z"
        }
    }
}
```
