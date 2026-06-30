/**
 * Defence GIS Tracking System
 * API Utility Module — Native fetch() based (no jQuery dependency)
 */
'use strict';

var API_BASE = '/DefenceGIS/api';

var API = {
    get: function(endpoint, params) {
        var url = API_BASE + endpoint;
        if (params) {
            var qs = Object.keys(params).map(function(k) {
                return encodeURIComponent(k) + '=' + encodeURIComponent(params[k]);
            }).join('&');
            if (qs) url += '?' + qs;
        }
        return fetch(url, { credentials: 'same-origin' })
            .then(function(r) { return r.json(); });
    },

    post: function(endpoint, data) {
        return fetch(API_BASE + endpoint, {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        }).then(function(r) { return r.json(); });
    },

    put: function(endpoint, data) {
        return fetch(API_BASE + endpoint, {
            method: 'PUT',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json' },
            body: data ? JSON.stringify(data) : undefined
        }).then(function(r) { return r.json(); });
    },

    del: function(endpoint) {
        return fetch(API_BASE + endpoint, {
            method: 'DELETE',
            credentials: 'same-origin'
        }).then(function(r) { return r.json(); });
    },

    handleError: function(err) {
        console.error('[API Error]', err);
        var toast = document.getElementById('toast');
        if (toast) {
            toast.textContent = 'Error: ' + (err.message || 'Request failed');
            toast.classList.add('show');
            setTimeout(function() { toast.classList.remove('show'); }, 4000);
        }
    }
};
