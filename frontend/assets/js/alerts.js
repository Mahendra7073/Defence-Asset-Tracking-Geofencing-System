/**
 * Defence GIS — Alerts Module (native fetch, no jQuery)
 */
'use strict';

var Alerts = {
    loadAlerts: function(filters) {
        var params = filters || {};
        API.get('/alerts', params)
            .then(function(res) {
                if (res.status === 'success') {
                    Alerts.renderTable(res.data);
                    Alerts.updateStats(res.data);
                }
            })
            .catch(API.handleError);
    },

    renderTable: function(alerts) {
        var tbody = document.getElementById('alert-table-body');
        if (!tbody) return;
        if (!alerts || alerts.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7"><div class="empty-state"><div class="empty-icon">🔔</div><div class="empty-title">No alerts found</div></div></td></tr>';
            return;
        }
        var html = '';
        alerts.forEach(function(a) {
            html += '<tr>';
            html += '<td>' + new Date(a.triggeredAt).toLocaleString('en-IN') + '</td>';
            html += '<td>' + (a.assetName || 'Asset-' + a.assetId) + '</td>';
            html += '<td>' + (a.zoneName || 'Zone-' + a.zoneId) + '</td>';
            html += '<td><span class="badge badge-' + (a.alertType === 'ENTER' ? 'danger' : a.alertType === 'EXIT' ? 'warning' : 'danger') + '">' + a.alertType + '</span></td>';
            html += '<td><span class="badge badge-' + Alerts.sevBadge(a.severity) + '">' + a.severity + '</span></td>';
            html += '<td><span class="badge badge-' + (a.acknowledged ? 'success' : 'danger') + '">' + (a.acknowledged ? 'Ack' : 'Open') + '</span></td>';
            html += '<td>';
            if (!a.acknowledged) {
                html += '<button class="btn btn-sm btn-primary" onclick="Alerts.acknowledge(' + a.id + ')">Ack</button>';
            } else {
                html += '<span style="color:var(--color-text-muted)">Resolved</span>';
            }
            html += '</td></tr>';
        });
        tbody.innerHTML = html;
    },

    updateStats: function(alerts) {
        var unack = 0, ack = 0, today = 0;
        var todayStr = new Date().toISOString().slice(0, 10);
        alerts.forEach(function(a) {
            if (a.acknowledged) ack++; else unack++;
            if (a.triggeredAt && a.triggeredAt.slice(0, 10) === todayStr) today++;
        });
        var e1 = document.getElementById('stat-unack'); if (e1) e1.textContent = unack;
        var e2 = document.getElementById('stat-today'); if (e2) e2.textContent = today;
        var e3 = document.getElementById('stat-ack'); if (e3) e3.textContent = ack;
        var badge = document.getElementById('alert-count'); if (badge) badge.textContent = unack;
    },

    acknowledge: function(alertId) {
        API.put('/alerts/' + alertId + '/acknowledge')
            .then(function() { Alerts.loadAlerts(); })
            .catch(API.handleError);
    },

    acknowledgeAll: function() {
        if (!confirm('Acknowledge all alerts?')) return;
        API.put('/alerts/acknowledge-all')
            .then(function() { Alerts.loadAlerts(); })
            .catch(API.handleError);
    },

    sevBadge: function(s) {
        switch(s) { case 'CRITICAL': case 'HIGH': return 'danger'; case 'MEDIUM': return 'warning'; default: return 'info'; }
    },

    startPolling: function(ms) {
        setInterval(function() { Alerts.loadAlerts(); }, ms || 15000);
    }
};

document.addEventListener('DOMContentLoaded', function() {
    Alerts.loadAlerts();
    Alerts.startPolling(15000);
    var filter = document.getElementById('alert-severity-filter');
    if (filter) filter.addEventListener('change', function() {
        Alerts.loadAlerts(this.value !== 'all' ? { severity: this.value } : {});
    });
    var ackAll = document.getElementById('btn-ack-all');
    if (ackAll) ackAll.addEventListener('click', Alerts.acknowledgeAll);
});
