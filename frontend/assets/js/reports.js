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

    exportExcel: function() {
        var period = document.getElementById('report-period');
        var val = period ? period.value : 'week';
        API.get('/alerts', { limit: 500, period: val })
            .then(function(res) {
                if (res.status !== 'success' || !res.data) return;
                
                // Try Excel export using SheetJS (XLSX)
                if (typeof XLSX !== 'undefined') {
                    try {
                        var excelData = [];
                        // 1. Add headers
                        excelData.push(["Time", "Asset", "Zone", "Type", "Severity", "Status"]);
                        
                        // 2. Add data rows formatted exactly like the UI table
                        res.data.forEach(function(a) {
                            excelData.push([
                                new Date(a.triggeredAt).toLocaleString('en-IN'),
                                a.assetName || ('Asset-' + a.assetId),
                                a.zoneName || ('Zone-' + a.zoneId),
                                a.alertType,
                                a.severity,
                                a.acknowledged ? 'Ack' : 'Open'
                            ]);
                        });
                        
                        // 3. Create worksheet and workbook
                        var ws = XLSX.utils.aoa_to_sheet(excelData);
                        
                        // 4. Auto-size columns based on maximum content length
                        var colWidths = excelData[0].map(function(col, i) {
                            return {
                                wch: Math.max.apply(null, excelData.map(function(row) {
                                    return row[i] ? row[i].toString().length : 0;
                                })) + 3 // add padding for spacing
                            };
                        });
                        ws['!cols'] = colWidths;
                        
                        var wb = XLSX.utils.book_new();
                        XLSX.utils.book_append_sheet(wb, ws, "Alert Report");
                        
                        // 5. Generate filename: Defence_GIS_Report_<YYYY-MM-DD_HH-MM>.xlsx
                        var now = new Date();
                        var pad = function(n) { return n < 10 ? '0' + n : n; };
                        var formattedDate = now.getFullYear() + '-' +
                                            pad(now.getMonth() + 1) + '-' +
                                            pad(now.getDate()) + '_' +
                                            pad(now.getHours()) + '-' +
                                            pad(now.getMinutes());
                        var filename = 'Defence_GIS_Report_' + formattedDate + '.xlsx';
                        
                        // 6. Write and download file
                        XLSX.writeFile(wb, filename);
                        if (typeof App !== 'undefined' && App.showToast) {
                            App.showToast('Report exported successfully as Excel.', 'success');
                        }
                        return; // Done
                    } catch (e) {
                        console.error('SheetJS export failed, falling back to CSV', e);
                    }
                }
                
                // Fallback to CSV if SheetJS is missing or fails
                var csv = 'Time,Asset,Zone,Type,Severity,Status\n';
                res.data.forEach(function(a) {
                    csv += new Date(a.triggeredAt).toLocaleString('en-IN') + ',' +
                           (a.assetName || ('Asset-' + a.assetId)) + ',' +
                           (a.zoneName || ('Zone-' + a.zoneId)) + ',' +
                           a.alertType + ',' +
                           a.severity + ',' +
                           (a.acknowledged ? 'Ack' : 'Open') + '\n';
                });
                var blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
                var url = URL.createObjectURL(blob);
                var link = document.createElement('a');
                link.href = url;
                
                var now = new Date();
                var pad = function(n) { return n < 10 ? '0' + n : n; };
                var formattedDate = now.getFullYear() + '-' +
                                    pad(now.getMonth() + 1) + '-' +
                                    pad(now.getDate()) + '_' +
                                    pad(now.getHours()) + '-' +
                                    pad(now.getMinutes());
                link.download = 'Defence_GIS_Report_' + formattedDate + '.csv';
                link.click();
                if (typeof App !== 'undefined' && App.showToast) {
                    App.showToast('Report exported as CSV (Excel library unavailable).', 'warning');
                }
            })
            .catch(API.handleError);
    }
};

document.addEventListener('DOMContentLoaded', function() {
    Reports.load();
    var period = document.getElementById('report-period');
    if (period) period.addEventListener('change', function() { Reports.load(); });
    var exportBtn = document.getElementById('btn-export-report');
    if (exportBtn) exportBtn.addEventListener('click', Reports.exportExcel);
});
