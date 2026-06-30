/**
 * Defence GIS — Auth Module (working implementation, no jQuery)
 */
'use strict';

var Auth = {
    checkSession: function() {
        return fetch(API_BASE + '/auth/session', { credentials: 'same-origin' })
            .then(function(r) { return r.json(); })
            .then(function(data) {
                if (data.status !== 'success') {
                    window.location.href = 'login.html';
                    return null;
                }
                var u = data.data;
                var nameEl = document.getElementById('user-display-name');
                var roleEl = document.getElementById('user-display-role');
                var avatarEl = document.getElementById('user-avatar');
                if (nameEl) nameEl.textContent = u.username || 'Admin';
                if (roleEl) roleEl.textContent = u.role || 'operator';
                if (avatarEl) avatarEl.textContent = (u.username || 'A').charAt(0).toUpperCase();
                return u;
            })
            .catch(function() { window.location.href = 'login.html'; });
    },

    logout: function() {
        fetch(API_BASE + '/auth/logout', { method: 'POST', credentials: 'same-origin' })
            .finally(function() { window.location.href = 'login.html'; });
    },

    initLogoutButton: function() {
        var btn = document.getElementById('btn-logout');
        if (btn) btn.addEventListener('click', Auth.logout);
    }
};
