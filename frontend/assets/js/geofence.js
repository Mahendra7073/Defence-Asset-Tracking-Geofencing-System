/**
 * Defence GIS — Geofence Module (native fetch, no jQuery)
 */
'use strict';

var Geofence = {
    loadZones: function() {
        API.get('/geofences')
            .then(function(res) {
                if (res.status === 'success' && res.data) {
                    var features = res.data.features || [];
                    Geofence.renderTable(features);
                    if (typeof MapController !== 'undefined') {
                        MapController.loadGeofenceZones();
                    }
                }
            })
            .catch(API.handleError);
    },

    renderTable: function(zones) {
        var tbody = document.getElementById('zone-table-body');
        if (!tbody) return;
        if (!zones || zones.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4"><div class="empty-state"><div class="empty-icon">📍</div><div class="empty-title">No zones defined</div></div></td></tr>';
            return;
        }
        var html = '';
        zones.forEach(function(z) {
            var p = z.properties || z;
            html += '<tr>';
            html += '<td><strong>' + (p.zoneName || '') + '</strong></td>';
            html += '<td><span class="badge badge-' + (p.zoneType === 'restricted' ? 'danger' : p.zoneType === 'safe' ? 'success' : 'warning') + '">' + (p.zoneType || '') + '</span></td>';
            html += '<td><span class="badge badge-' + (p.isActive !== false ? 'success' : 'danger') + '">' + (p.isActive !== false ? 'Active' : 'Inactive') + '</span></td>';
            html += '<td><button class="btn btn-sm btn-secondary" onclick="Geofence.zoomTo(' + (p.id || 0) + ')">View</button></td>';
            html += '</tr>';
        });
        tbody.innerHTML = html;
    },

    zoomTo: function(id) {
        if (typeof MapController !== 'undefined') {
            MapController.highlightAndZoomTo(id);
        }
    }
};

document.addEventListener('DOMContentLoaded', function() {
    if (typeof MapController !== 'undefined') {
        MapController.init('geofence-map');
    }
    Geofence.loadZones();
});
