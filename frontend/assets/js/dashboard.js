/**
 * Defence GIS Tracking System
 * Dashboard Module — loads KPI stats, renders mini-map, populates recent alerts.
 * Dependencies: utils.js, map.js
 */
'use strict';

var Dashboard = {
    loadStats: function() {
        API.get('/dashboard')
            .then(function(res) {
                if (res.status === 'success') {
                    document.getElementById('stat-total-assets').textContent = res.data.totalAssets || 0;
                    document.getElementById('stat-active-assets').textContent = res.data.activeAssets || 0;
                    document.getElementById('stat-zones').textContent = res.data.totalZones || 0;
                    document.getElementById('stat-alerts').textContent = res.data.unacknowledgedAlerts || 0;
                    var badge = document.getElementById('alert-count');
                    if (badge) badge.textContent = res.data.unacknowledgedAlerts || 0;
                }
            })
            .catch(API.handleError);
    },

    initMiniMap: function() {
        MapController.init('dashboard-map');
        MapController.loadAssetMarkers();
        MapController.loadGeofenceZones();
    },

    loadRecentAlerts: function() {
        API.get('/alerts', { limit: 10 })
            .then(function(res) {
                var container = document.getElementById('recent-alerts-container');
                if (!container) return;
                if (res.status === 'success' && res.data && res.data.length > 0) {
                    var html = '<table class="data-table"><thead><tr><th>Time</th><th>Asset</th><th>Type</th><th>Severity</th></tr></thead><tbody>';
                    res.data.forEach(function(alert) {
                        html += '<tr>';
                        html += '<td>' + new Date(alert.triggeredAt).toLocaleString('en-IN') + '</td>';
                        html += '<td>' + (alert.assetName || alert.assetId) + '</td>';
                        html += '<td><span class="badge badge-' + (alert.alertType === 'ENTER' ? 'danger' : 'warning') + '">' + alert.alertType + '</span></td>';
                        html += '<td><span class="badge badge-' + Dashboard.severityClass(alert.severity) + '">' + alert.severity + '</span></td>';
                        html += '</tr>';
                    });
                    html += '</tbody></table>';
                    container.innerHTML = html;
                }
            })
            .catch(API.handleError);
    },

    severityClass: function(severity) {
        switch (severity) {
            case 'CRITICAL': case 'HIGH': return 'danger';
            case 'MEDIUM': return 'warning';
            default: return 'info';
        }
    },

    startAutoRefresh: function(intervalMs) {
        setInterval(function() {
            Dashboard.loadStats();
            Dashboard.loadRecentAlerts();
        }, intervalMs || 30000);
    }
};
