# GeoServer Setup Guide

## Prerequisites

- GeoServer 2.25.x installed (standalone or Docker)
- PostgreSQL 18 + PostGIS 3.6 running
- Database `defence_gis` created and schema loaded

---

## Step 1: Create Workspace

1. Open GeoServer Admin: `http://localhost:8080/geoserver`
2. Navigate to **Workspaces → Add New Workspace**
3. Enter:
   - Name: `defence`
   - Namespace URI: `http://defence.drdo.gis`
4. Check **Default Workspace**
5. Click **Submit**

---

## Step 2: Create PostGIS Store

1. Navigate to **Stores → Add New Store → PostGIS**
2. Configure:

| Parameter     | Value          |
| ------------- | -------------- |
| Workspace     | `defence`      |
| Store Name    | `defence_gis`  |
| Host          | `localhost`    |
| Port          | `5432`         |
| Database      | `defence_gis`  |
| Schema        | `public`       |
| User          | `postgres`     |
| Password      | *(your password)* |

3. Click **Save**

---

## Step 3: Publish Layers

For each of the following tables, click **Publish**:

### Layer: `asset_positions`
- Geometry Type: Point
- SRS: `EPSG:4326`
- Compute bounding box from data

### Layer: `geofence_zones`
- Geometry Type: Polygon
- SRS: `EPSG:4326`
- Compute bounding box from data

### Layer: `track_history`
- Geometry Type: LineString
- SRS: `EPSG:4326`
- Compute bounding box from data

---

## Step 4: Verify

### WMS URL
```
http://localhost:8080/geoserver/defence/wms
```

### WFS URL
```
http://localhost:8080/geoserver/defence/wfs
```

### Test WFS GeoJSON Output
```
http://localhost:8080/geoserver/defence/wfs?service=WFS&version=2.0.0&request=GetFeature&typeNames=defence:geofence_zones&outputFormat=application/json
```

---

## Step 5: Apply SLD Styles

Apply the SLD style files from the `geoserver/styles/` directory:

| Style File    | Target Layer      | Description                     |
| ------------- | ----------------- | ------------------------------- |
| `assets.sld`  | `asset_positions` | Point markers for assets        |
| `tracks.sld`  | `track_history`   | Line styling for route history  |
| `zones.sld`   | `geofence_zones`  | Polygon fill for geofence zones |

To apply: navigate to **Styles → Add a new style**, upload the `.sld` file, and assign it to the corresponding layer under **Layers → Publishing → Default Style**.

