package com.drdo.gis.servlet;

import com.drdo.gis.service.GpsSimulatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.*;

/**
 * SimulationBootstrap — starts all simulation engines when Tomcat deploys the WAR.
 *
 * Engines started:
 *  1. GpsSimulatorService  — updates asset positions every GPS_INTERVAL_SEC seconds.
 *
 * Event-Driven Alerts:
 *  - Alerts are now generated dynamically when events happen inside GpsSimulatorService.
 */
@WebListener
public class SimulationBootstrap implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(SimulationBootstrap.class);

    /** GPS update every N seconds (default 30). */
    private static final int GPS_INTERVAL_SEC =
        Integer.parseInt(System.getProperty("gps.interval.sec", "30"));

    private ScheduledExecutorService gpsExecutor;
    private final GpsSimulatorService gpsService = new GpsSimulatorService();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("=============================================================");
        log.info("  Defence GIS — Simulation Bootstrap STARTING");
        log.info("  GPS interval   : {} seconds", GPS_INTERVAL_SEC);
        log.info("  Alerts mode    : EVENT-DRIVEN (No time-based polling alerts)");
        log.info("=============================================================");

        // ── GPS Simulator — fixed rate every GPS_INTERVAL_SEC ──────────────
        gpsExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "gps-simulator");
            t.setDaemon(true);
            return t;
        });
        gpsExecutor.scheduleAtFixedRate(
            this::runGpsTick,
            5,                    // initial delay (seconds) — let Tomcat finish startup
            GPS_INTERVAL_SEC,
            TimeUnit.SECONDS
        );

        log.info("Simulation engines started successfully.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Simulation Bootstrap — shutting down executors...");
        shutdownExecutor(gpsExecutor, "GPS");
        log.info("Simulation Bootstrap — shutdown complete.");
    }

    private void runGpsTick() {
        try {
            gpsService.tick();
        } catch (Throwable t) {
            log.error("GPS simulator tick error: {}", t.getMessage(), t);
        }
    }

    private void shutdownExecutor(ScheduledExecutorService exec, String name) {
        if (exec == null) return;
        exec.shutdown();
        try {
            if (!exec.awaitTermination(5, TimeUnit.SECONDS)) {
                exec.shutdownNow();
                log.warn("{} executor forced shutdown after timeout", name);
            }
        } catch (InterruptedException e) {
            exec.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
