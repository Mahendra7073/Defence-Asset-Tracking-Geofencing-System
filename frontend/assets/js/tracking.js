/**
 * Defence GIS — Tracking / Route History Module (native fetch, no jQuery)
 */
'use strict';

var Tracking = {
    routePolyline: null,
    routePoints: [],
    playbackIndex: 0,
    playbackTimer: null,
    movingMarker: null,

    loadRoute: function() {
        var assetSel = document.getElementById('route-asset-select');
        var startEl = document.getElementById('route-date-start');
        var endEl = document.getElementById('route-date-end');
        var assetId = assetSel ? assetSel.value : '';
        var start = startEl ? startEl.value : '';
        var end = endEl ? endEl.value : '';
        if (!assetId) { alert('Please select an asset.'); return; }

        API.get('/tracks', { assetId: assetId, start: start, end: end })
            .then(function(res) {
                if (res.status === 'success' && res.data) {
                    Tracking.renderRoute(res.data);
                    Tracking.updateDetails(res.data.properties || res.data);
                }
            })
            .catch(API.handleError);
    },

    loadAssetOptions: function() {
        var sel = document.getElementById('route-asset-select');
        if (!sel) return;
        API.get('/assets')
            .then(function(res) {
                if (res.status === 'success' && res.data) {
                    res.data.forEach(function(a) {
                        var opt = document.createElement('option');
                        opt.value = a.id;
                        opt.textContent = a.assetName + ' (' + a.assetCode + ')';
                        sel.appendChild(opt);
                    });
                }
            })
            .catch(API.handleError);
    },

    renderRoute: function(geojson) {
        if (!MapController || !MapController.map) return;
        if (Tracking.routePolyline) MapController.map.removeLayer(Tracking.routePolyline);
        if (geojson.geometry && geojson.geometry.coordinates) {
            Tracking.routePoints = geojson.geometry.coordinates.map(function(c) { return [c[1], c[0]]; });
            Tracking.routePolyline = L.polyline(Tracking.routePoints, {
                color: '#00d4ff', weight: 3, opacity: 0.8
            }).addTo(MapController.map);
            MapController.map.fitBounds(Tracking.routePolyline.getBounds(), { padding: [40, 40] });
            var slider = document.getElementById('route-slider');
            if (slider) slider.max = Tracking.routePoints.length - 1;
        }
    },

    updateDetails: function(props) {
        var d = document.getElementById('route-distance');
        var s = document.getElementById('route-speed');
        var dur = document.getElementById('route-duration');
        var pts = document.getElementById('route-points');
        if (d) d.textContent = ((props.distanceM || 0) / 1000).toFixed(2) + ' km';
        if (s) s.textContent = (props.avgSpeed || 0).toFixed(1) + ' km/h';
        if (pts) pts.textContent = props.pointCount || 0;
        if (dur && props.startedAt && props.endedAt) {
            var ms = new Date(props.endedAt) - new Date(props.startedAt);
            var mins = Math.floor(ms / 60000);
            dur.textContent = Math.floor(mins / 60) + 'h ' + (mins % 60) + 'm';
        }
    },

    play: function() {
        if (Tracking.routePoints.length === 0) return;
        if (Tracking.playbackTimer) clearInterval(Tracking.playbackTimer);
        Tracking.playbackTimer = setInterval(function() {
            if (Tracking.playbackIndex >= Tracking.routePoints.length) {
                clearInterval(Tracking.playbackTimer);
                return;
            }
            var pos = Tracking.routePoints[Tracking.playbackIndex];
            if (!Tracking.movingMarker) {
                Tracking.movingMarker = L.circleMarker(pos, {
                    radius: 6, color: '#00d4ff', fillColor: '#00d4ff', fillOpacity: 1
                }).addTo(MapController.map);
            } else {
                Tracking.movingMarker.setLatLng(pos);
            }
            var slider = document.getElementById('route-slider');
            if (slider) slider.value = Tracking.playbackIndex;
            Tracking.playbackIndex++;
        }, 200);
    },

    pause: function() { if (Tracking.playbackTimer) clearInterval(Tracking.playbackTimer); },

    reset: function() {
        Tracking.pause();
        Tracking.playbackIndex = 0;
        if (Tracking.movingMarker && MapController.map) MapController.map.removeLayer(Tracking.movingMarker);
        Tracking.movingMarker = null;
        var slider = document.getElementById('route-slider');
        if (slider) slider.value = 0;
    }
};

document.addEventListener('DOMContentLoaded', function() {
    if (typeof MapController !== 'undefined') MapController.init('route-map');
    Tracking.loadAssetOptions();
    var btnLoad = document.getElementById('btn-load-route');
    if (btnLoad) btnLoad.addEventListener('click', Tracking.loadRoute);
    var btnPlay = document.getElementById('btn-play-route');
    if (btnPlay) btnPlay.addEventListener('click', Tracking.play);
    var btnPause = document.getElementById('btn-pause-route');
    if (btnPause) btnPause.addEventListener('click', Tracking.pause);
    var btnReset = document.getElementById('btn-reset-route');
    if (btnReset) btnReset.addEventListener('click', Tracking.reset);
});
