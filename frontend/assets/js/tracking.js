/**
 * Defence GIS — Tracking / Route History Module
 *
 * Features:
 *  - Load GPS track from API (last 24h default or date range)
 *  - Draw polyline with start (green) and end (red) markers
 *  - Animated playback with speed control (0.5×, 1×, 2×, 4×)
 *  - Play / Pause / Resume / Stop / Restart controls
 *  - Live timestamp and distance-travelled display during playback
 *  - Slider scrubbing
 */
'use strict';

var Tracking = {
    // ── State ────────────────────────────────────────────────────────────────
    routePolyline:   null,
    routePoints:     [],      // [[lat, lng], ...]
    timestamps:      [],      // ISO strings, one per point
    speeds:          [],      // speed at each point
    playbackIndex:   0,
    playbackTimer:   null,
    interpTimer:     null,    // sub-tick interpolation timer
    movingMarker:    null,
    startMarker:     null,
    endMarker:       null,
    totalDistM:      0,
    distanceTravelled: 0,
    isPlaying:       false,

    // ── Speed multiplier map ─────────────────────────────────────────────────
    SPEED_MAP: { '0.5': 400, '1': 200, '2': 100, '4': 50 }, // ms per step

    // ────────────────────────────────────────────────────────────────────────
    // Route Loading
    // ────────────────────────────────────────────────────────────────────────

    loadRoute: function() {
        var assetSel  = document.getElementById('route-asset-select');
        var startEl   = document.getElementById('route-date-start');
        var endEl     = document.getElementById('route-date-end');
        var assetId   = assetSel ? assetSel.value : '';
        var start     = startEl  ? startEl.value  : '';
        var end       = endEl    ? endEl.value     : '';

        if (!assetId) { alert('Please select an asset.'); return; }
        if (start && end && new Date(start) > new Date(end)) {
            if (typeof App !== 'undefined' && App.showToast) {
                App.showToast('Start date cannot be after end date.', 'warning');
            } else {
                alert('Start date cannot be after end date.');
            }
            return;
        }

        Tracking.reset();
        Tracking.setStatus('Loading route data…');

        var params = { assetId: assetId };
        if (start) params.start = start;
        if (end)   params.end   = end;

        API.get('/tracks', params)
            .then(function(res) {
                if (res.status === 'success' && res.data && res.data.geometry) {
                    Tracking.renderRoute(res.data);
                    Tracking.updateDetails(res.data.properties || {});
                    Tracking.setStatus('Route loaded — ' + Tracking.routePoints.length + ' GPS points. Press ▶ Play to animate.');
                } else {
                    Tracking.setStatus('No GPS data found for this asset / date range.');
                }
            })
            .catch(function(err) {
                Tracking.setStatus('Error loading route: ' + (err.message || err));
            });
    },

    loadAssetOptions: function() {
        var sel = document.getElementById('route-asset-select');
        if (!sel) return;
        API.get('/assets')
            .then(function(res) {
                if (res.status === 'success' && res.data) {
                    res.data.forEach(function(a) {
                        var opt = document.createElement('option');
                        opt.value       = a.id;
                        opt.textContent = a.assetName + ' (' + a.assetCode + ')';
                        sel.appendChild(opt);
                    });
                }
            })
            .catch(API.handleError);
    },

    // ────────────────────────────────────────────────────────────────────────
    // Map Rendering
    // ────────────────────────────────────────────────────────────────────────

    renderRoute: function(geojson) {
        if (!MapController || !MapController.map) return;

        var coords = geojson.geometry.coordinates; // [[lng, lat], ...]
        if (!coords || coords.length === 0) return;

        // Convert to [lat, lng] for Leaflet
        Tracking.routePoints = coords.map(function(c) { return [c[1], c[0]]; });

        // Store properties
        var props = geojson.properties || {};
        Tracking.timestamps   = props.timestamps || [];
        Tracking.speeds       = props.speeds || [];
        Tracking.totalDistM   = props.distanceM  || 0;
        Tracking.distanceTravelled = 0;

        // Draw polyline
        if (Tracking.routePolyline) MapController.map.removeLayer(Tracking.routePolyline);
        Tracking.routePolyline = L.polyline(Tracking.routePoints, {
            color: '#00d4ff', weight: 3, opacity: 0.8,
            dashArray: null
        }).addTo(MapController.map);
        MapController.map.fitBounds(Tracking.routePolyline.getBounds(), { padding: [40, 40] });

        // Start marker — green flag
        if (Tracking.startMarker) MapController.map.removeLayer(Tracking.startMarker);
        var startIcon = L.divIcon({
            className: '',
            html: '<div style="width:20px;height:20px;background:#00e676;border:2px solid #fff;border-radius:50% 50% 50% 0;transform:rotate(-45deg);box-shadow:0 2px 6px rgba(0,0,0,0.5)"></div>',
            iconSize: [20, 20], iconAnchor: [10, 20]
        });
        Tracking.startMarker = L.marker(Tracking.routePoints[0], { icon: startIcon })
            .bindPopup('<strong>🟢 Start</strong><br>' + (Tracking.timestamps[0] ? new Date(Tracking.timestamps[0]).toLocaleString('en-IN') : '—'))
            .addTo(MapController.map);

        // End marker — red flag
        if (Tracking.endMarker) MapController.map.removeLayer(Tracking.endMarker);
        var endPt   = Tracking.routePoints[Tracking.routePoints.length - 1];
        var endIcon = L.divIcon({
            className: '',
            html: '<div style="width:20px;height:20px;background:#ff5252;border:2px solid #fff;border-radius:50% 50% 50% 0;transform:rotate(-45deg);box-shadow:0 2px 6px rgba(0,0,0,0.5)"></div>',
            iconSize: [20, 20], iconAnchor: [10, 20]
        });
        var endTs = Tracking.timestamps[Tracking.timestamps.length - 1];
        Tracking.endMarker = L.marker(endPt, { icon: endIcon })
            .bindPopup('<strong>🔴 End</strong><br>' + (endTs ? new Date(endTs).toLocaleString('en-IN') : '—'))
            .addTo(MapController.map);

        // Set slider range
        var slider = document.getElementById('route-slider');
        if (slider) {
            slider.max   = Tracking.routePoints.length - 1;
            slider.value = 0;
        }

        // Initialize progress UI
        var progressEl = document.getElementById('route-progress');
        if (progressEl) progressEl.textContent = '0 / ' + Tracking.routePoints.length;
    },

    updateDetails: function(props) {
        var d   = document.getElementById('route-distance');
        var s   = document.getElementById('route-speed');
        var dur = document.getElementById('route-duration');
        var pts = document.getElementById('route-points');

        if (d)   d.textContent  = ((props.distanceM || 0) / 1000).toFixed(2) + ' km';
        if (s)   s.textContent  = (props.avgSpeed || 0).toFixed(1) + ' km/h';
        if (pts) pts.textContent = props.pointCount || 0;

        if (dur && props.startedAt && props.endedAt) {
            var ms   = new Date(props.endedAt) - new Date(props.startedAt);
            var mins = Math.floor(ms / 60000);
            dur.textContent = Math.floor(mins / 60) + 'h ' + (mins % 60) + 'm';
        }
    },

    // ────────────────────────────────────────────────────────────────────────
    // Playback Controls
    // ────────────────────────────────────────────────────────────────────────

    play: function() {
        if (Tracking.routePoints.length === 0) {
            Tracking.setStatus('Load a route first.'); return;
        }
        if (Tracking.isPlaying) return; // already playing
        Tracking.isPlaying = true;
        Tracking.runPlaybackStep();
    },

    runPlaybackStep: function() {
        if (!Tracking.isPlaying) return;
        if (Tracking.playbackIndex >= Tracking.routePoints.length) {
            Tracking.isPlaying = false;
            Tracking.setStatus('Playback complete. Press ↺ Restart to replay.');
            return;
        }

        var speedSel = document.getElementById('route-speed');
        var speedVal = speedSel ? speedSel.value : '1';
        var delay    = Tracking.SPEED_MAP[speedVal] || 200;

        var posStart = Tracking.playbackIndex > 0 ? Tracking.routePoints[Tracking.playbackIndex - 1] : Tracking.routePoints[0];
        var posEnd   = Tracking.routePoints[Tracking.playbackIndex];

        // Reset previous sub-step interpolator if any
        if (Tracking.interpTimer) {
            clearInterval(Tracking.interpTimer);
            Tracking.interpTimer = null;
        }

        // Initialize moving marker if needed
        if (!Tracking.movingMarker) {
            var icon = L.divIcon({
                className: '',
                html: '<div style="width:16px;height:16px;background:#ffab00;border:3px solid #fff;border-radius:50%;box-shadow:0 0 10px rgba(255,171,0,0.85); transition: transform 0.05s linear;"></div>',
                iconSize: [16, 16], iconAnchor: [8, 8]
            });
            Tracking.movingMarker = L.marker(posStart, { icon: icon }).addTo(MapController.map);
        }

        // Interpolation specs
        var steps = 10; // 10 steps of transition between coordinates
        var stepInterval = delay / steps;
        var step = 0;

        Tracking.interpTimer = setInterval(function() {
            if (!Tracking.isPlaying) {
                clearInterval(Tracking.interpTimer);
                return;
            }
            step++;
            var t   = step / steps;
            var lat = posStart[0] + (posEnd[0] - posStart[0]) * t;
            var lng = posStart[1] + (posEnd[1] - posStart[1]) * t;
            var currentPos = [lat, lng];

            // Set marker coordinate
            Tracking.movingMarker.setLatLng(currentPos);

            // Display Coordinates
            var coordEl = document.getElementById('route-current-coords');
            if (coordEl) coordEl.textContent = lat.toFixed(5) + ', ' + lng.toFixed(5);

            // Interpolate Speed
            var speedStart = Tracking.playbackIndex > 0 ? (Tracking.speeds[Tracking.playbackIndex - 1] || 0) : 0;
            var speedEnd   = Tracking.speeds[Tracking.playbackIndex] || 0;
            var currentSpeed = speedStart + (speedEnd - speedStart) * t;
            var speedEl = document.getElementById('route-current-speed');
            if (speedEl) speedEl.textContent = currentSpeed.toFixed(1) + ' km/h';

            if (step >= steps) {
                clearInterval(Tracking.interpTimer);
                Tracking.interpTimer = null;

                // Accumulate distance
                if (Tracking.playbackIndex > 0) {
                    var prev = Tracking.routePoints[Tracking.playbackIndex - 1];
                    Tracking.distanceTravelled += Tracking.latLngDistM(prev, posEnd);
                    var distEl = document.getElementById('route-dist-travelled');
                    if (distEl) distEl.textContent = (Tracking.distanceTravelled / 1000).toFixed(3) + ' km';
                }

                // Display timestamp
                if (Tracking.timestamps[Tracking.playbackIndex]) {
                    var tsEl = document.getElementById('route-current-time');
                    if (tsEl) tsEl.textContent = new Date(Tracking.timestamps[Tracking.playbackIndex]).toLocaleString('en-IN');
                }

                // Slider sync
                var slider = document.getElementById('route-slider');
                if (slider) slider.value = Tracking.playbackIndex;

                // Progress display
                var progressEl = document.getElementById('route-progress');
                if (progressEl) progressEl.textContent = (Tracking.playbackIndex + 1) + ' / ' + Tracking.routePoints.length;

                // Advance index and schedule next coordinate
                Tracking.playbackIndex++;
                Tracking.playbackTimer = setTimeout(Tracking.runPlaybackStep, 10);
            }
        }, stepInterval);
    },

    pause: function() {
        Tracking.isPlaying = false;
        if (Tracking.playbackTimer) {
            clearTimeout(Tracking.playbackTimer);
            Tracking.playbackTimer = null;
        }
        if (Tracking.interpTimer) {
            clearInterval(Tracking.interpTimer);
            Tracking.interpTimer = null;
        }
        Tracking.setStatus('Paused at point ' + Tracking.playbackIndex + ' / ' + Tracking.routePoints.length + '. Press ▶ Play to resume.');
    },

    reset: function() {
        Tracking.pause();
        Tracking.playbackIndex     = 0;
        Tracking.distanceTravelled = 0;

        if (Tracking.movingMarker && MapController.map)
            MapController.map.removeLayer(Tracking.movingMarker);
        Tracking.movingMarker = null;

        if (Tracking.startMarker && MapController.map)
            MapController.map.removeLayer(Tracking.startMarker);
        Tracking.startMarker = null;

        if (Tracking.endMarker && MapController.map)
            MapController.map.removeLayer(Tracking.endMarker);
        Tracking.endMarker = null;

        if (Tracking.routePolyline && MapController.map)
            MapController.map.removeLayer(Tracking.routePolyline);
        Tracking.routePolyline = null;

        Tracking.routePoints = [];
        Tracking.timestamps  = [];
        Tracking.speeds      = [];

        var slider = document.getElementById('route-slider');
        if (slider) slider.value = 0;

        var tsEl = document.getElementById('route-current-time');
        if (tsEl) tsEl.textContent = '—';

        var distEl = document.getElementById('route-dist-travelled');
        if (distEl) distEl.textContent = '0 km';

        var coordEl = document.getElementById('route-current-coords');
        if (coordEl) coordEl.textContent = '—';

        var speedEl = document.getElementById('route-current-speed');
        if (speedEl) speedEl.textContent = '—';

        var progressEl = document.getElementById('route-progress');
        if (progressEl) progressEl.textContent = '0 / 0';

        // Issue 6: Reset stats panel values
        var d = document.getElementById('route-distance'); if (d) d.textContent = '—';
        var s = document.getElementById('route-speed');    if (s) s.textContent = '—';
        var dur = document.getElementById('route-duration'); if (dur) dur.textContent = '—';
        var pts = document.getElementById('route-points');   if (pts) pts.textContent = '—';

        Tracking.setStatus('Ready. Select an asset and click Load Route.');
    },

    restart: function() {
        Tracking.pause();
        Tracking.playbackIndex     = 0;
        Tracking.distanceTravelled = 0;
        if (Tracking.movingMarker && MapController.map)
            MapController.map.removeLayer(Tracking.movingMarker);
        Tracking.movingMarker = null;
        var slider = document.getElementById('route-slider');
        if (slider) slider.value = 0;
        var tsEl = document.getElementById('route-current-time');
        if (tsEl) tsEl.textContent = '—';
        var distEl = document.getElementById('route-dist-travelled');
        if (distEl) distEl.textContent = '0 km';
        var coordEl = document.getElementById('route-current-coords');
        if (coordEl) coordEl.textContent = '—';
        var speedEl = document.getElementById('route-current-speed');
        if (speedEl) speedEl.textContent = '—';
        var progressEl = document.getElementById('route-progress');
        if (progressEl) progressEl.textContent = '0 / ' + Tracking.routePoints.length;
        Tracking.setStatus('Restarted. Press ▶ Play.');
    },

    // Slider scrub
    scrubTo: function(idx) {
        Tracking.pause();
        Tracking.playbackIndex = Math.max(0, Math.min(parseInt(idx), Tracking.routePoints.length - 1));
        var pos = Tracking.routePoints[Tracking.playbackIndex];

        // Issue 2: Create moving marker if it doesn't exist yet
        if (pos && !Tracking.movingMarker && MapController && MapController.map) {
            var icon = L.divIcon({
                className: '',
                html: '<div style="width:16px;height:16px;background:#ffab00;border:3px solid #fff;border-radius:50%;box-shadow:0 0 10px rgba(255,171,0,0.85);"></div>',
                iconSize: [16, 16], iconAnchor: [8, 8]
            });
            Tracking.movingMarker = L.marker(pos, { icon: icon }).addTo(MapController.map);
        }
        if (pos && Tracking.movingMarker) {
            Tracking.movingMarker.setLatLng(pos);
        }

        // Issue 3: Recalculate distance travelled up to scrubbed position
        Tracking.distanceTravelled = 0;
        for (var i = 1; i <= Tracking.playbackIndex; i++) {
            Tracking.distanceTravelled += Tracking.latLngDistM(Tracking.routePoints[i - 1], Tracking.routePoints[i]);
        }
        var distEl = document.getElementById('route-dist-travelled');
        if (distEl) distEl.textContent = (Tracking.distanceTravelled / 1000).toFixed(3) + ' km';

        if (Tracking.timestamps[Tracking.playbackIndex]) {
            var tsEl = document.getElementById('route-current-time');
            if (tsEl) tsEl.textContent = new Date(Tracking.timestamps[Tracking.playbackIndex]).toLocaleString('en-IN');
        }
        var coordEl = document.getElementById('route-current-coords');
        if (coordEl && pos) coordEl.textContent = pos[0].toFixed(5) + ', ' + pos[1].toFixed(5);

        var speedEl = document.getElementById('route-current-speed');
        if (speedEl) speedEl.textContent = (Tracking.speeds[Tracking.playbackIndex] || 0).toFixed(1) + ' km/h';

        var progressEl = document.getElementById('route-progress');
        if (progressEl) progressEl.textContent = (Tracking.playbackIndex + 1) + ' / ' + Tracking.routePoints.length;
    },

    // ── Utility ──────────────────────────────────────────────────────────────

    /** Haversine distance between two [lat, lng] pairs in metres. */
    latLngDistM: function(a, b) {
        var R    = 6371000;
        var dLat = (b[0] - a[0]) * Math.PI / 180;
        var dLng = (b[1] - a[1]) * Math.PI / 180;
        var sinA = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(a[0] * Math.PI / 180) * Math.cos(b[0] * Math.PI / 180)
                 * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(sinA), Math.sqrt(1 - sinA));
    },

    setStatus: function(msg) {
        var el = document.getElementById('route-status');
        if (el) el.textContent = msg;
    }
};

// ── DOM ready ─────────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', function() {
    if (typeof MapController !== 'undefined') MapController.init('route-map');

    // Default date range: today and yesterday
    var today     = new Date();
    var yesterday = new Date(today); yesterday.setDate(today.getDate() - 1);
    var fmt = function(d) { return d.toISOString().split('T')[0]; };
    var startEl = document.getElementById('route-date-start');
    var endEl   = document.getElementById('route-date-end');
    if (startEl) startEl.value = fmt(yesterday);
    if (endEl)   endEl.value   = fmt(today);

    Tracking.loadAssetOptions();
    Tracking.setStatus('Ready. Select an asset and click Load Route.');

    // Wire buttons
    var btnLoad    = document.getElementById('btn-load-route');
    var btnPlay    = document.getElementById('btn-play-route');
    var btnPause   = document.getElementById('btn-pause-route');
    var btnReset   = document.getElementById('btn-reset-route');
    var btnRestart = document.getElementById('btn-restart-route');
    var slider     = document.getElementById('route-slider');

    if (btnLoad)    btnLoad.addEventListener('click',  Tracking.loadRoute);
    if (btnPlay)    btnPlay.addEventListener('click',  Tracking.play);
    if (btnPause)   btnPause.addEventListener('click', Tracking.pause);
    if (btnReset)   btnReset.addEventListener('click', Tracking.reset);
    if (btnRestart) btnRestart.addEventListener('click', Tracking.restart);
    if (slider)     slider.addEventListener('input', function() { Tracking.scrubTo(this.value); });
});


