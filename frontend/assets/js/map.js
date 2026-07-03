/**
 * Defence GIS — Leaflet Map Controller
 * Working implementation: initializes L.map, loads asset markers from API,
 * loads geofence zones from API, adds GeoServer WMS layer.
 */
'use strict';

var API_BASE = '/DefenceGIS/api';

var MapController = {
    map: null,
    assetLayer: null,
    zoneLayer: null,
    wmsLayer: null,
    zoneLayers: {},
    animationInterval: null,

    init: function(containerId) {
        if (this.map) return;
        var self = this;
        this.zoneLayers = {};
        this.animationInterval = null;
        
        this.map = L.map(containerId, {
            center: [26.2389, 73.0243],
            zoom: 13,
            zoomControl: true
        });

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; OpenStreetMap contributors',
            maxZoom: 19
        }).addTo(this.map);

        L.control.scale({ imperial: false }).addTo(this.map);

        this.assetLayer = L.layerGroup().addTo(this.map);
        this.zoneLayer = L.layerGroup().addTo(this.map);

        this.map.on('popupclose', function() {
            self.resetZoneStyles();
        });
    },

    loadAssetMarkers: function() {
        var self = this;
        fetch(API_BASE + '/positions/latest', { credentials: 'same-origin' })
            .then(function(r) { return r.json(); })
            .then(function(res) {
                if (res.status !== 'success' || !res.data) return;
                self.assetLayer.clearLayers();
                var geojson = res.data;
                L.geoJSON(geojson, {
                    pointToLayer: function(feature, latlng) {
                        var color = '#448aff';
                        var shape = 'circle';
                        switch (feature.properties.assetType) {
                            case 'vehicle': color = '#448aff'; break;
                            case 'tank':    color = '#ff5252'; break;
                            case 'drone':   color = '#00e676'; break;
                            case 'person':  color = '#ffab40'; break;
                        }
                        return L.circleMarker(latlng, {
                            radius: 8, fillColor: color, color: '#fff',
                            weight: 2, fillOpacity: 0.9
                        });
                    },
                    onEachFeature: function(feature, layer) {
                        var p = feature.properties;
                        layer.bindPopup(
                            '<strong>' + p.assetName + '</strong><br>' +
                            'Type: ' + p.assetType + '<br>' +
                            'Code: ' + p.assetCode + '<br>' +
                            'Speed: ' + (p.speed || 0).toFixed(1) + ' km/h<br>' +
                            'Time: ' + (p.recordedAt || '—')
                        );
                    }
                }).addTo(self.assetLayer);
            })
            .catch(function(err) {
                console.error('Failed to load asset markers:', err);
            });
    },

    loadGeofenceZones: function() {
        var self = this;
        fetch(API_BASE + '/geofences', { credentials: 'same-origin' })
            .then(function(r) { return r.json(); })
            .then(function(res) {
                if (res.status !== 'success' || !res.data) return;
                self.zoneLayer.clearLayers();
                self.zoneLayers = {};
                L.geoJSON(res.data, {
                    style: function(feature) {
                        var color = feature.properties.color || '#ff0000';
                        return {
                            color: color, weight: 2, opacity: 0.8,
                            fillColor: color, fillOpacity: 0.15
                        };
                    },
                    onEachFeature: function(feature, layer) {
                        var p = feature.properties;
                        self.zoneLayers[p.id] = layer;
                        layer.on('click', function(e) {
                            L.DomEvent.stopPropagation(e);
                            self.highlightAndZoomTo(p.id);
                        });
                    }
                }).addTo(self.zoneLayer);
            })
            .catch(function(err) {
                console.error('Failed to load geofence zones:', err);
            });
    },

    highlightAndZoomTo: function(id) {
        var self = this;
        var selectedLayer = self.zoneLayers[id];
        if (!selectedLayer) return;

        // Zoom map to the selected zone
        self.map.fitBounds(selectedLayer.getBounds());

        // Highlight only that polygon and fade others
        self.highlightZone(id);

        // Fetch details from backend and open popup
        fetch(API_BASE + '/geofences/' + id, { credentials: 'same-origin' })
            .then(function(r) { return r.json(); })
            .then(function(res) {
                if (res.status !== 'success' || !res.data) return;
                var data = res.data;
                var createdDate = data.createdAt ? new Date(data.createdAt).toLocaleString('en-IN') : '—';
                
                // Format coordinates nicely
                var coords = data.coordinates || '';
                if (coords.length > 80) {
                    coords = coords.substring(0, 77) + '...';
                }

                var popupContent = 
                    '<div class="geofence-popup" style="color: var(--color-text-primary); min-width: 250px;">' +
                    '  <h3 style="margin: 0 0 8px 0; color: var(--color-accent); font-size: 14px; border-bottom: 1px solid var(--color-card-border); padding-bottom: 4px;">' + data.zoneName + '</h3>' +
                    '  <table style="width: 100%; font-size: 12px; border-collapse: collapse;">' +
                    '    <tr style="border-bottom: 1px solid rgba(255,255,255,0.05);"><td style="padding: 4px 0; color: var(--color-text-muted);">Type:</td><td style="padding: 4px 0; font-weight: bold; text-transform: capitalize;">' + data.zoneType + '</td></tr>' +
                    '    <tr style="border-bottom: 1px solid rgba(255,255,255,0.05);"><td style="padding: 4px 0; color: var(--color-text-muted);">Status:</td><td style="padding: 4px 0; font-weight: bold; color: var(--color-success); text-transform: uppercase;">' + data.status + '</td></tr>' +
                    '    <tr style="border-bottom: 1px solid rgba(255,255,255,0.05);"><td style="padding: 4px 0; color: var(--color-text-muted);">Created:</td><td style="padding: 4px 0;">' + createdDate + '</td></tr>' +
                    '    <tr style="border-bottom: 1px solid rgba(255,255,255,0.05);"><td style="padding: 4px 0; color: var(--color-text-muted);">Area:</td><td style="padding: 4px 0; font-weight: bold;">' + Number(data.area).toLocaleString('en-IN') + ' m²</td></tr>' +
                    '    <tr style="border-bottom: 1px solid rgba(255,255,255,0.05);"><td style="padding: 4px 0; color: var(--color-text-muted);">Assets Inside:</td><td style="padding: 4px 0; font-weight: bold;">' + data.assetsInsideCount + '</td></tr>' +
                    '    <tr style="border-bottom: 1px solid rgba(255,255,255,0.05);"><td style="padding: 4px 0; color: var(--color-text-muted);">Alerts Inside:</td><td style="padding: 4px 0; font-weight: bold; color: ' + (data.alertsCount > 0 ? 'var(--color-danger)' : 'var(--color-text-primary)') + '">' + data.alertsCount + '</td></tr>' +
                    '    <tr><td style="padding: 4px 0; color: var(--color-text-muted); vertical-align: top;">Coordinates:</td><td style="padding: 4px 0; font-size: 10px; font-family: monospace; word-break: break-all; white-space: normal;">' + coords + '</td></tr>' +
                    '  </table>' +
                    '</div>';

                selectedLayer.bindPopup(popupContent).openPopup(selectedLayer.getBounds().getCenter());
            })
            .catch(function(err) {
                console.error('Failed to load geofence details:', err);
            });
    },

    highlightZone: function(id) {
        var self = this;
        // Stop current animation if any
        if (self.animationInterval) {
            clearInterval(self.animationInterval);
            self.animationInterval = null;
        }

        var selectedLayer = self.zoneLayers[id];
        if (!selectedLayer) return;

        // Apply highlight to the selected zone and fade all other zones
        for (var zoneId in self.zoneLayers) {
            var layer = self.zoneLayers[zoneId];
            if (parseInt(zoneId) === id) {
                var color = layer.feature.properties.color || '#ff0000';
                layer.setStyle({
                    color: color,
                    weight: 5,        // Thicker border
                    opacity: 1.0,     // Solid border
                    fillColor: color,
                    fillOpacity: 0.5  // Brighter fill
                });
                layer.bringToFront();
            } else {
                layer.setStyle({
                    opacity: 0.15,
                    fillOpacity: 0.05
                });
            }
        }

        // Animate the selected polygon's border weight to create a pulse effect
        var baseWeight = 5;
        var step = 0;
        self.animationInterval = setInterval(function() {
            step += 1;
            var weight = baseWeight + Math.sin(step * 0.4) * 2.5; // oscillate between 2.5 and 7.5
            selectedLayer.setStyle({ weight: weight });
        }, 100);
    },

    resetZoneStyles: function() {
        var self = this;
        if (self.animationInterval) {
            clearInterval(self.animationInterval);
            self.animationInterval = null;
        }
        for (var zoneId in self.zoneLayers) {
            var layer = self.zoneLayers[zoneId];
            var color = layer.feature.properties.color || '#ff0000';
            layer.setStyle({
                color: color,
                weight: 2,
                opacity: 0.8,
                fillColor: color,
                fillOpacity: 0.15
            });
        }
    },

    addWmsLayer: function() {
        if (this.wmsLayer) return;
        this.wmsLayer = L.tileLayer.wms('http://localhost:8085/geoserver/defence/wms', {
            layers: 'defence:asset_positions',
            format: 'image/png',
            transparent: true,
            attribution: 'GeoServer'
        }).addTo(this.map);
    },

    centerOn: function(lat, lng, zoom) {
        if (this.map) this.map.setView([lat, lng], zoom || 15);
    },

    startAutoRefresh: function(intervalMs) {
        var self = this;
        setInterval(function() {
            self.loadAssetMarkers();
        }, intervalMs || 10000);
    }
};
