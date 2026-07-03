#!/bin/bash
# ============================================================
# GeoServer Automated Setup via REST API — Docker Version
# Defence Asset Tracking & Geofencing System
# ============================================================
# This script is called after docker compose up to configure
# GeoServer with workspace, datastore, layers, and styles.
# ============================================================

set -e

GEOSERVER_URL="${GEOSERVER_URL:-http://localhost:8085/geoserver}"
GS_USER="${GS_USER:-admin}"
GS_PASS="${GS_PASS:-geoserver}"
WORKSPACE="defence"
DATASTORE="defence_postgis"
DB_HOST="${DB_HOST:-postgres}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-defence_gis}"
DB_USER="${DB_USER:-postgres}"
DB_PASS="${DB_PASS:-postgres}"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
STYLES_DIR="${SCRIPT_DIR}/../../geoserver/styles"

echo "============================================"
echo " GeoServer Docker Setup — Defence GIS"
echo "============================================"

# Wait for GeoServer to be ready
echo "[0/6] Waiting for GeoServer to be ready..."
MAX_RETRIES=60
RETRY=0
until curl -sf -u "$GS_USER:$GS_PASS" "$GEOSERVER_URL/rest/about/version.json" > /dev/null 2>&1; do
    RETRY=$((RETRY + 1))
    if [ $RETRY -ge $MAX_RETRIES ]; then
        echo "ERROR: GeoServer did not become ready after ${MAX_RETRIES} attempts."
        exit 1
    fi
    echo "  Waiting... (attempt $RETRY/$MAX_RETRIES)"
    sleep 5
done
echo "  GeoServer is ready!"

# 1. Create Workspace
echo "[1/6] Creating workspace '$WORKSPACE'..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -u "$GS_USER:$GS_PASS" \
    "$GEOSERVER_URL/rest/workspaces/$WORKSPACE.json")

if [ "$HTTP_CODE" = "200" ]; then
    echo "  Workspace '$WORKSPACE' already exists. Skipping."
else
    curl -s -u "$GS_USER:$GS_PASS" -XPOST "$GEOSERVER_URL/rest/workspaces" \
        -H "Content-type: application/json" \
        -d "{\"workspace\":{\"name\":\"$WORKSPACE\"}}" \
        -o /dev/null -w "  HTTP %{http_code}\n"
fi

# 2. Create PostGIS Datastore
echo "[2/6] Creating PostGIS datastore '$DATASTORE'..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -u "$GS_USER:$GS_PASS" \
    "$GEOSERVER_URL/rest/workspaces/$WORKSPACE/datastores/$DATASTORE.json")

if [ "$HTTP_CODE" = "200" ]; then
    echo "  Datastore '$DATASTORE' already exists. Skipping."
else
    curl -s -u "$GS_USER:$GS_PASS" -XPOST \
        "$GEOSERVER_URL/rest/workspaces/$WORKSPACE/datastores" \
        -H "Content-type: application/json" \
        -d "{
            \"dataStore\": {
                \"name\": \"$DATASTORE\",
                \"type\": \"PostGIS\",
                \"connectionParameters\": {
                    \"entry\": [
                        {\"@key\": \"host\",     \"\$\": \"$DB_HOST\"},
                        {\"@key\": \"port\",     \"\$\": \"$DB_PORT\"},
                        {\"@key\": \"database\", \"\$\": \"$DB_NAME\"},
                        {\"@key\": \"user\",     \"\$\": \"$DB_USER\"},
                        {\"@key\": \"passwd\",   \"\$\": \"$DB_PASS\"},
                        {\"@key\": \"dbtype\",   \"\$\": \"postgis\"},
                        {\"@key\": \"schema\",   \"\$\": \"public\"}
                    ]
                }
            }
        }" -o /dev/null -w "  HTTP %{http_code}\n"
fi

# 3. Publish Layers
echo "[3/6] Publishing layers..."
for LAYER in asset_positions geofence_zones track_history; do
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -u "$GS_USER:$GS_PASS" \
        "$GEOSERVER_URL/rest/workspaces/$WORKSPACE/datastores/$DATASTORE/featuretypes/$LAYER.json")

    if [ "$HTTP_CODE" = "200" ]; then
        echo "  Layer '$LAYER' already exists. Skipping."
    else
        echo "  Publishing layer '$LAYER'..."
        curl -s -u "$GS_USER:$GS_PASS" -XPOST \
            "$GEOSERVER_URL/rest/workspaces/$WORKSPACE/datastores/$DATASTORE/featuretypes" \
            -H "Content-type: application/json" \
            -d "{
                \"featureType\": {
                    \"name\": \"$LAYER\",
                    \"nativeName\": \"$LAYER\",
                    \"srs\": \"EPSG:4326\",
                    \"enabled\": true
                }
            }" -o /dev/null -w "  HTTP %{http_code}\n"
    fi
done

# 4. Upload SLD Styles
echo "[4/6] Uploading SLD styles..."
for SLD_FILE in assets.sld zones.sld tracks.sld; do
    STYLE_NAME="${SLD_FILE%.sld}"

    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -u "$GS_USER:$GS_PASS" \
        "$GEOSERVER_URL/rest/workspaces/$WORKSPACE/styles/$STYLE_NAME.json")

    if [ "$HTTP_CODE" = "200" ]; then
        echo "  Style '$STYLE_NAME' already exists. Skipping."
    else
        echo "  Creating style '$STYLE_NAME'..."
        # Create style entry
        curl -s -u "$GS_USER:$GS_PASS" -XPOST \
            "$GEOSERVER_URL/rest/workspaces/$WORKSPACE/styles" \
            -H "Content-type: application/json" \
            -d "{\"style\":{\"name\":\"$STYLE_NAME\",\"filename\":\"$SLD_FILE\"}}" \
            -o /dev/null -w "  HTTP %{http_code}\n"

        # Upload SLD content
        if [ -f "$STYLES_DIR/$SLD_FILE" ]; then
            curl -s -u "$GS_USER:$GS_PASS" -XPUT \
                "$GEOSERVER_URL/rest/workspaces/$WORKSPACE/styles/$STYLE_NAME" \
                -H "Content-type: application/vnd.ogc.sld+xml" \
                -d @"$STYLES_DIR/$SLD_FILE" \
                -o /dev/null -w "  HTTP %{http_code}\n"
        else
            echo "  WARNING: SLD file not found: $STYLES_DIR/$SLD_FILE"
        fi
    fi
done

# 5. Assign styles to layers
echo "[5/6] Assigning default styles to layers..."
for LAYER in asset_positions geofence_zones track_history; do
    if [ "$LAYER" = "asset_positions" ]; then
        STYLE="assets"
    elif [ "$LAYER" = "geofence_zones" ]; then
        STYLE="zones"
    else
        STYLE="tracks"
    fi
    echo "  Assigning style '$STYLE' to layer '$LAYER'..."
    curl -s -u "$GS_USER:$GS_PASS" -XPUT \
        "$GEOSERVER_URL/rest/layers/$WORKSPACE:$LAYER" \
        -H "Content-type: application/json" \
        -d "{\"layer\":{\"defaultStyle\":{\"name\":\"$WORKSPACE:$STYLE\"}}}" \
        -o /dev/null -w "  HTTP %{http_code}\n"
done

# 6. Verify
echo "[6/6] Verifying setup..."
echo ""
echo "  Workspace:"
curl -s -u "$GS_USER:$GS_PASS" "$GEOSERVER_URL/rest/workspaces/$WORKSPACE.json" 2>/dev/null | grep -o '"name":"[^"]*"' || echo "  (check manually)"
echo ""
echo "  Layers:"
curl -s -u "$GS_USER:$GS_PASS" "$GEOSERVER_URL/rest/workspaces/$WORKSPACE/layers.json" 2>/dev/null | grep -o '"name":"[^"]*"' || echo "  (check manually)"
echo ""
echo "============================================"
echo " GeoServer setup complete!"
echo ""
echo " WMS: $GEOSERVER_URL/$WORKSPACE/wms"
echo " WFS: $GEOSERVER_URL/$WORKSPACE/wfs"
echo "============================================"

