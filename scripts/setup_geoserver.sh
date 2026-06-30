#!/bin/bash
# ============================================================
# GeoServer Automated Setup via REST API
# Defence Asset Tracking & Geofencing System
# ============================================================
# Prerequisites: GeoServer running at http://localhost:8080/geoserver
# Default credentials: admin/geoserver

GEOSERVER_URL="http://localhost:8080/geoserver"
GS_USER="admin"
GS_PASS="geoserver"
WORKSPACE="defence"
DATASTORE="defence_postgis"
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="defence_gis"
DB_USER="postgres"
DB_PASS="postgres"

echo "============================================"
echo " GeoServer Setup — Defence GIS"
echo "============================================"

# 1. Create Workspace
echo "[1/5] Creating workspace '$WORKSPACE'..."
curl -s -u $GS_USER:$GS_PASS -XPOST $GEOSERVER_URL/rest/workspaces \
  -H "Content-type: application/json" \
  -d "{\"workspace\":{\"name\":\"$WORKSPACE\"}}" \
  -o /dev/null -w "HTTP %{http_code}\n"

# 2. Create PostGIS Datastore
echo "[2/5] Creating PostGIS datastore..."
curl -s -u $GS_USER:$GS_PASS -XPOST \
  $GEOSERVER_URL/rest/workspaces/$WORKSPACE/datastores \
  -H "Content-type: application/json" \
  -d "{
    \"dataStore\": {
      \"name\": \"$DATASTORE\",
      \"type\": \"PostGIS\",
      \"connectionParameters\": {
        \"entry\": [
          {\"@key\": \"host\", \"\$\": \"$DB_HOST\"},
          {\"@key\": \"port\", \"\$\": \"$DB_PORT\"},
          {\"@key\": \"database\", \"\$\": \"$DB_NAME\"},
          {\"@key\": \"user\", \"\$\": \"$DB_USER\"},
          {\"@key\": \"passwd\", \"\$\": \"$DB_PASS\"},
          {\"@key\": \"dbtype\", \"\$\": \"postgis\"},
          {\"@key\": \"schema\", \"\$\": \"public\"}
        ]
      }
    }
  }" -o /dev/null -w "HTTP %{http_code}\n"

# 3. Publish Layers
for LAYER in asset_positions geofence_zones track_history; do
  echo "[3/5] Publishing layer '$LAYER'..."
  curl -s -u $GS_USER:$GS_PASS -XPOST \
    $GEOSERVER_URL/rest/workspaces/$WORKSPACE/datastores/$DATASTORE/featuretypes \
    -H "Content-type: application/json" \
    -d "{
      \"featureType\": {
        \"name\": \"$LAYER\",
        \"nativeName\": \"$LAYER\",
        \"srs\": \"EPSG:4326\",
        \"enabled\": true
      }
    }" -o /dev/null -w "HTTP %{http_code}\n"
done

# 4. Upload SLD Styles
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
STYLES_DIR="$SCRIPT_DIR/../geoserver/styles"

for SLD_FILE in assets.sld zones.sld tracks.sld; do
  STYLE_NAME="${SLD_FILE%.sld}"
  echo "[4/5] Uploading style '$STYLE_NAME'..."
  # Create style entry
  curl -s -u $GS_USER:$GS_PASS -XPOST \
    $GEOSERVER_URL/rest/workspaces/$WORKSPACE/styles \
    -H "Content-type: application/json" \
    -d "{\"style\":{\"name\":\"$STYLE_NAME\",\"filename\":\"$SLD_FILE\"}}" \
    -o /dev/null -w "HTTP %{http_code}\n"
  # Upload SLD content
  if [ -f "$STYLES_DIR/$SLD_FILE" ]; then
    curl -s -u $GS_USER:$GS_PASS -XPUT \
      $GEOSERVER_URL/rest/workspaces/$WORKSPACE/styles/$STYLE_NAME \
      -H "Content-type: application/vnd.ogc.sld+xml" \
      -d @"$STYLES_DIR/$SLD_FILE" \
      -o /dev/null -w "HTTP %{http_code}\n"
  fi
done

# 5. Verify
echo "[5/5] Verifying..."
echo "  Workspace: $(curl -s -u $GS_USER:$GS_PASS $GEOSERVER_URL/rest/workspaces/$WORKSPACE.json | grep -o '\"name":"[^"]*"')"
echo "  Layers: $(curl -s -u $GS_USER:$GS_PASS $GEOSERVER_URL/rest/workspaces/$WORKSPACE/layers.json)"
echo ""
echo "WMS Test URL:"
echo "  $GEOSERVER_URL/$WORKSPACE/wms?service=WMS&request=GetMap&layers=$WORKSPACE:asset_positions&bbox=72.9,26.1,73.1,26.4&width=768&height=768&srs=EPSG:4326&format=image/png"
echo ""
echo "Setup complete!"
