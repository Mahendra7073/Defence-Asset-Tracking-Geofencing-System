/**
 * Defence GIS Tracking System
 * Main Application Controller
 * Handles: sidebar state, nav highlighting, global clock
 */
'use strict';

var App = {
    init: function() {
        this.initSidebar();
        this.highlightActiveNav();
        this.initHeaderClock();
    },

    initSidebar: function() {
        var toggleBtn = document.getElementById('sidebar-toggle');
        var sidebar = document.getElementById('sidebar');
        if (toggleBtn && sidebar) {
            toggleBtn.addEventListener('click', function() {
                sidebar.classList.toggle('collapsed');
            });
        }
    },

    highlightActiveNav: function() {
        var currentPath = window.location.pathname.split('/').pop() || 'index.html';
        var navItems = document.querySelectorAll('.nav-item');
        navItems.forEach(function(item) {
            var href = item.getAttribute('href');
            if (href && href.includes(currentPath)) {
                item.classList.add('active');
            }
        });
    },

    initHeaderClock: function() {
        var clockEl = document.getElementById('header-clock');
        if (!clockEl) return;
        function updateClock() {
            clockEl.textContent = new Date().toLocaleTimeString('en-IN', {
                hour: '2-digit', minute: '2-digit', second: '2-digit',
                hour12: false, timeZone: 'Asia/Kolkata'
            });
        }
        updateClock();
        setInterval(updateClock, 1000);
    }
};

document.addEventListener('DOMContentLoaded', function() {
    App.init();
});
