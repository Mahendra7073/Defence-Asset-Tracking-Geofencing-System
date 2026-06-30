# GeoServer Configuration

This directory contains GeoServer setup files, SLD styles, and automation scripts.

## Directory Structure

```
geoserver/
├── README.md               ← This file
├── setup_guide.md           ← GeoServer setup instructions
├── styles/                  ← SLD style descriptors
│   └── (pending)
└── scripts/                 ← Automation scripts
    └── (pending)
```

## GeoServer Details

| Setting        | Value                                         |
| -------------- | --------------------------------------------- |
| Workspace      | `defence`                                     |
| Store Type     | PostGIS                                       |
| Store Name     | `defence_gis`                                 |
| CRS            | `EPSG:4326`                                   |
| WMS URL        | `http://localhost:8080/geoserver/defence/wms`  |
| WFS URL        | `http://localhost:8080/geoserver/defence/wfs`  |

## Published Layers

| Layer Name          | Geometry    | Description               |
| ------------------- | ----------- | ------------------------- |
| `asset_positions`   | Point       | Live asset GPS locations  |
| `geofence_zones`    | Polygon     | Restricted/safe zones     |
| `track_history`     | LineString  | Historical movement paths |
