/**
 * Defence GIS — Reports Module (native fetch, Chart.js)
 */
'use strict';

var Reports = {
    alertChart: null,
    activityChart: null,

    load: function() {
        var period = document.getElementById('report-period');
        var val = period ? period.value : 'week';
        API.get('/alerts', { limit: 100, period: val })
            .then(function(res) {
                if (res.status === 'success' && res.data) {
                    Reports.updateStats(res.data);
                    Reports.renderAlertChart(res.data);
                    Reports.renderTable(res.data);
                }
            })
            .catch(API.handleError);

        API.get('/dashboard')
            .then(function(res) {
                if (res.status === 'success') {
                    var el = document.getElementById('rpt-positions');
                    if (el) el.textContent = res.data.activeAssets || 0;
                }
            })
            .catch(API.handleError);
    },

    updateStats: function(alerts) {
        var breaches = 0;
        alerts.forEach(function(a) { if (a.alertType === 'ENTER') breaches++; });
        var el1 = document.getElementById('rpt-alerts'); if (el1) el1.textContent = alerts.length;
        var el2 = document.getElementById('rpt-breaches'); if (el2) el2.textContent = breaches;
    },

    renderAlertChart: function(alerts) {
        var canvas = document.getElementById('chart-alerts-daily');
        if (!canvas || typeof Chart === 'undefined') return;

        var dayMap = {};
        alerts.forEach(function(a) {
            var day = a.triggeredAt ? a.triggeredAt.slice(0, 10) : 'unknown';
            dayMap[day] = (dayMap[day] || 0) + 1;
        });
        var labels = Object.keys(dayMap).sort();
        var data = labels.map(function(l) { return dayMap[l]; });

        if (Reports.alertChart) Reports.alertChart.destroy();
        Reports.alertChart = new Chart(canvas, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Alerts',
                    data: data,
                    backgroundColor: 'rgba(255,82,82,0.7)',
                    borderColor: '#ff5252',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { display: false } },
                scales: {
                    y: { beginAtZero: true, ticks: { color: '#8a9bb5' }, grid: { color: 'rgba(138,155,181,0.1)' } },
                    x: { ticks: { color: '#8a9bb5' }, grid: { display: false } }
                }
            }
        });
    },

    renderTable: function(alerts) {
        var tbody = document.getElementById('report-table-body');
        if (!tbody) return;
        if (!alerts || alerts.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6"><div class="empty-state"><div class="empty-icon">📋</div><div class="empty-title">No data</div></div></td></tr>';
            return;
        }
        var html = '';
        alerts.slice(0, 50).forEach(function(a) {
            html += '<tr>';
            html += '<td>' + new Date(a.triggeredAt).toLocaleString('en-IN') + '</td>';
            html += '<td>' + (a.assetName || 'Asset-' + a.assetId) + '</td>';
            html += '<td>' + (a.zoneName || 'Zone-' + a.zoneId) + '</td>';
            html += '<td>' + a.alertType + '</td>';
            html += '<td><span class="badge badge-' + (a.severity === 'HIGH' ? 'danger' : 'warning') + '">' + a.severity + '</span></td>';
            html += '<td><span class="badge badge-' + (a.acknowledged ? 'success' : 'danger') + '">' + (a.acknowledged ? 'Ack' : 'Open') + '</span></td>';
            html += '</tr>';
        });
        tbody.innerHTML = html;
    },

    exportCSV: function() {
        var period = document.getElementById('report-period');
        var val = period ? period.value : 'week';
        API.get('/alerts', { limit: 500, period: val })
            .then(function(res) {
                if (res.status !== 'success' || !res.data) return;
                var csv = 'DateTime,Asset,Zone,Type,Severity,Status\n';
                res.data.forEach(function(a) {
                    csv += a.triggeredAt + ',' + (a.assetName || '') + ',' + (a.zoneName || '') + ',' + a.alertType + ',' + a.severity + ',' + (a.acknowledged ? 'Ack' : 'Open') + '\n';
                });
                var blob = new Blob([csv], { type: 'text/csv' });
                var url = URL.createObjectURL(blob);
                var link = document.createElement('a');
                link.href = url;
                link.download = 'defence_gis_report_' + new Date().toISOString().slice(0, 10) + '.csv';
                link.click();
            })
            .catch(API.handleError);
    }
};

document.addEventListener('DOMContentLoaded', function() {
    Reports.load();
    var period = document.getElementById('report-period');
    if (period) period.addEventListener('change', function() { Reports.load(); });
    var exportBtn = document.getElementById('btn-export-report');
    if (exportBtn) exportBtn.addEventListener('click', Reports.exportCSV);
});
