# GeoServer Configuration

This directory contains GeoServer setup files, SLD styles, and configuration references.

## Directory Structure

```
geoserver/
├── README.md               ← This file
├── setup_guide.md          ← GeoServer setup instructions
└── styles/                 ← SLD style descriptors
    ├── assets.sld          ← Asset point marker styling
    ├── tracks.sld          ← Track history line styling
    └── zones.sld           ← Geofence polygon zone styling
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

