var App = {
    init: function() {
        this.initSidebar();
        this.highlightActiveNav();
        this.initHeaderClock();
        this.initToastContainer();
        this.initKeyboardShortcuts();
        
        // Speed up dashboard and alerts page polling to 5 seconds
        this.adjustPollingIntervals();
    },

    initSidebar: function() {
        var toggleBtn = document.getElementById('sidebar-toggle');
        var sidebar = document.getElementById('sidebar');
        if (toggleBtn && sidebar) {
            toggleBtn.addEventListener('click', function() {
                sidebar.classList.toggle('collapsed');
                // Adjust maps if present
                if (typeof MapController !== 'undefined' && MapController.map) {
                    setTimeout(function() { MapController.map.invalidateSize(); }, 300);
                }
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
    },

    initToastContainer: function() {
        if (!document.getElementById('toast-container')) {
            var container = document.createElement('div');
            container.id = 'toast-container';
            document.body.appendChild(container);
        }
    },

    showToast: function(message, type) {
        var container = document.getElementById('toast-container');
        if (!container) return;

        var toast = document.createElement('div');
        toast.className = 'toast-alert ' + (type || 'info');
        toast.innerHTML = '<span>' + message + '</span><span class="toast-close">×</span>';

        toast.querySelector('.toast-close').onclick = function() {
            toast.style.animation = 'slideOut 0.3s ease forwards';
            setTimeout(function() { toast.remove(); }, 300);
        };

        container.appendChild(toast);

        // Auto remove after 5 seconds
        setTimeout(function() {
            if (toast.parentNode) {
                toast.style.animation = 'slideOut 0.3s ease forwards';
                setTimeout(function() { toast.remove(); }, 300);
            }
        }, 5000);
    },

    initKeyboardShortcuts: function() {
        document.addEventListener('keydown', function(e) {
            // F key toggles fullscreen if map exists
            if (e.key.toLowerCase() === 'f' && e.target.tagName !== 'INPUT' && e.target.tagName !== 'SELECT') {
                var mapEl = document.getElementById('tracking-map') || document.getElementById('route-map') || document.getElementById('dashboard-map');
                if (mapEl) {
                    if (document.fullscreenElement) {
                        document.exitFullscreen();
                    } else {
                        mapEl.requestFullscreen();
                    }
                }
            }
        });
    },

    adjustPollingIntervals: function() {
        // Adjust Dashboard & Alerts polling intervals to 5 seconds to show updates instantly
        if (typeof Dashboard !== 'undefined' && Dashboard.startAutoRefresh) {
            // Overwrite original auto refresh with 5000ms
            Dashboard.startAutoRefresh = function() {
                setInterval(function() {
                    Dashboard.loadStats();
                    Dashboard.loadRecentAlerts();
                }, 5000);
            };
        }
        if (typeof Alerts !== 'undefined' && Alerts.startAutoRefresh) {
            // Overwrite original alerts load and display toast alert for new unacknowledged alerts
            var oldLoadAlerts = Alerts.loadAlerts;
            var prevCount = -1;

            Alerts.loadAlerts = function(filters) {
                API.get('/alerts', filters || {})
                    .then(function(res) {
                        if (res.status === 'success') {
                            Alerts.renderTable(res.data);
                            Alerts.updateStats(res.data);

                            var unack = res.data.filter(a => !a.acknowledged);
                            if (prevCount !== -1 && unack.length > prevCount) {
                                // Trigger toast for the newest alerts
                                var diff = unack.length - prevCount;
                                for (var i = 0; i < diff; i++) {
                                    var alert = unack[i];
                                    if (alert) {
                                        var severityLabel = alert.severity === 'CRITICAL' ? '🚨 SOS' : '⚠️ ALERT';
                                        App.showToast(severityLabel + ': ' + (alert.assetName || ('Asset ' + alert.assetId)) + ' - ' + alert.alertType, alert.severity === 'CRITICAL' ? 'danger' : 'warning');
                                    }
                                }
                            }
                            prevCount = unack.length;
                        }
                    })
                    .catch(API.handleError);
            };

            Alerts.startPolling = function() {
                setInterval(function() { Alerts.loadAlerts(); }, 5000);
            };
        }
    }
};

document.addEventListener('DOMContentLoaded', function() {
    App.init();
});
