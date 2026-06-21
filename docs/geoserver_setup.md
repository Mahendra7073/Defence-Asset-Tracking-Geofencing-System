Workspace: defence

Store Type: PostGIS

Store Name: defence_gis

Database Name: defence_gis

Published Layers:
- asset_positions
- geofence_zones
- track_history

CRS:
EPSG:4326



WMS URL:
http://localhost:8080/geoserver/defence/wms

WFS URL:
http://localhost:8080/geoserver/defence/wfs

Layers:
defence:asset_positions
defence:geofence_zones
defence:track_history



Meaning of each layer:
asset_positions  -> live asset locations (geometry:Point)
geofence_zones   -> restricted/safe zones (geometry:Polygon)
track_history    -> movement routes (geometry:LineString)
