/**
 * Defence GIS — Assets Module (native fetch, no jQuery)
 */
'use strict';

var Assets = {
    loadAll: function(typeFilter) {
        var params = {};
        if (typeFilter && typeFilter !== 'all') params.type = typeFilter;
        API.get('/assets', params)
            .then(function(res) {
                if (res.status === 'success') {
                    Assets.renderTable(res.data);
                    Assets.updateCounts(res.data);
                }
            })
            .catch(API.handleError);
    },

    renderTable: function(assets) {
        var tbody = document.getElementById('asset-table-body');
        if (!tbody) return;
        if (!assets || assets.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6"><div class="empty-state"><div class="empty-icon">🚁</div><div class="empty-title">No assets found</div></div></td></tr>';
            return;
        }
        var html = '';
        assets.forEach(function(a) {
            html += '<tr>';
            html += '<td><strong>' + (a.assetCode || '') + '</strong></td>';
            html += '<td>' + (a.assetName || '') + '</td>';
            html += '<td><span class="badge badge-info">' + (a.assetType || '') + '</span></td>';
            html += '<td><span class="badge badge-' + (a.status === 'active' ? 'success' : 'danger') + '">' + (a.status || '') + '</span></td>';
            html += '<td>' + (a.createdAt ? new Date(a.createdAt).toLocaleDateString('en-IN') : '—') + '</td>';
            html += '<td><button class="btn btn-sm btn-secondary" onclick="Assets.edit(' + a.id + ')">Edit</button> ';
            html += '<button class="btn btn-sm btn-danger" onclick="Assets.remove(' + a.id + ')">Del</button></td>';
            html += '</tr>';
        });
        tbody.innerHTML = html;
    },

    updateCounts: function(assets) {
        var counts = { vehicle: 0, drone: 0, person: 0, tank: 0 };
        assets.forEach(function(a) { if (counts.hasOwnProperty(a.assetType)) counts[a.assetType]++; });
        ['vehicle', 'drone', 'person', 'tank'].forEach(function(t) {
            var el = document.getElementById('count-' + t);
            if (el) el.textContent = counts[t];
        });
    },

    create: function(data) {
        API.post('/assets', data)
            .then(function() { Assets.loadAll(); })
            .catch(API.handleError);
    },

    edit: function(id) {
        var name = prompt('New asset name:');
        if (!name) return;
        API.put('/assets/' + id, { assetName: name })
            .then(function() { Assets.loadAll(); })
            .catch(API.handleError);
    },

    remove: function(id) {
        if (!confirm('Delete this asset?')) return;
        API.del('/assets/' + id)
            .then(function() { Assets.loadAll(); })
            .catch(API.handleError);
    }
};

document.addEventListener('DOMContentLoaded', function() {
    Assets.loadAll();
    var filter = document.getElementById('filter-asset-type');
    if (filter) filter.addEventListener('change', function() { Assets.loadAll(this.value); });
});
