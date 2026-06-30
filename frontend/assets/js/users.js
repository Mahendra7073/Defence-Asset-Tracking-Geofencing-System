/**
 * Defence GIS — Users Module (native fetch, no jQuery)
 */
'use strict';

var Users = {
    loadAll: function() {
        API.get('/users')
            .then(function(res) {
                if (res.status === 'success' && res.data) {
                    Users.renderTable(res.data);
                    Users.updateStats(res.data);
                }
            })
            .catch(function() {
                var tbody = document.getElementById('user-table-body');
                if (tbody) tbody.innerHTML = '<tr><td colspan="7"><div class="empty-state"><div class="empty-icon">👤</div><div class="empty-title">User API not available</div><div class="empty-text">UserServlet may not be deployed yet.</div></div></td></tr>';
            });
    },

    renderTable: function(users) {
        var tbody = document.getElementById('user-table-body');
        if (!tbody) return;
        if (!users || users.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7"><div class="empty-state"><div class="empty-icon">👤</div><div class="empty-title">No users</div></div></td></tr>';
            return;
        }
        var html = '';
        users.forEach(function(u) {
            html += '<tr>';
            html += '<td><strong>' + (u.username || '') + '</strong></td>';
            html += '<td>' + (u.fullName || '') + '</td>';
            html += '<td>' + (u.email || '') + '</td>';
            html += '<td><span class="badge badge-info">' + (u.role || '') + '</span></td>';
            html += '<td><span class="badge badge-' + (u.active !== false ? 'success' : 'danger') + '">' + (u.active !== false ? 'Active' : 'Inactive') + '</span></td>';
            html += '<td>' + (u.lastLogin ? new Date(u.lastLogin).toLocaleString('en-IN') : '—') + '</td>';
            html += '<td><button class="btn btn-sm btn-secondary" onclick="Users.toggleRole(' + u.id + ',\'' + u.role + '\')">Role</button></td>';
            html += '</tr>';
        });
        tbody.innerHTML = html;
    },

    updateStats: function(users) {
        var total = users.length;
        var active = users.filter(function(u) { return u.active !== false; }).length;
        var e1 = document.getElementById('stat-total-users'); if (e1) e1.textContent = total;
        var e2 = document.getElementById('stat-active-users'); if (e2) e2.textContent = active;
    },

    toggleRole: function(id, currentRole) {
        var roles = ['admin', 'operator', 'viewer'];
        var idx = roles.indexOf(currentRole);
        var newRole = roles[(idx + 1) % roles.length];
        if (!confirm('Change role to ' + newRole + '?')) return;
        API.put('/users/' + id, { role: newRole })
            .then(function() { Users.loadAll(); })
            .catch(API.handleError);
    }
};

document.addEventListener('DOMContentLoaded', function() {
    Users.loadAll();
});
