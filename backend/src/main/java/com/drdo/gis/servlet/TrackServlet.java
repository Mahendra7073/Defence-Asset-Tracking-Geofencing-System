package com.drdo.gis.servlet;

import com.drdo.gis.config.DatabaseConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TrackServlet — returns route history as GeoJSON LineString with per-point timestamps.
 *
 * GET /api/tracks?assetId=1
 *   → returns last 24 hours of positions (default)
 *
 * GET /api/tracks?assetId=1&start=2026-06-01&end=2026-06-22
 *   → returns positions within the date range
 *
 * Response includes:
 *   data.geometry.coordinates  — [[lng, lat], ...]
 *   data.properties.timestamps — ["2026-07-06T10:00:00", ...] (one per coordinate)
 *   data.properties.pointCount, distanceM, avgSpeed, startedAt, endedAt
 */
@WebServlet("/api/tracks")
public class TrackServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(TrackServlet.class);
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        JsonObject result = new JsonObject();

        String assetIdStr = req.getParameter("assetId");
        String start = req.getParameter("start");
        String end   = req.getParameter("end");

        if (assetIdStr == null || assetIdStr.isEmpty()) {
            result.addProperty("status", "error");
            result.addProperty("message", "assetId is required");
            resp.setStatus(400);
            resp.getWriter().write(result.toString());
            return;
        }

        int assetId = Integer.parseInt(assetIdStr);

        // ── Step 1: fetch individual points with timestamps (for playback) ──
        List<double[]>  coords     = new ArrayList<>();
        List<String>    timestamps = new ArrayList<>();
        List<Double>    speeds     = new ArrayList<>();

        String pointsSql;
        if (start != null && !start.isEmpty() && end != null && !end.isEmpty()) {
            pointsSql = "SELECT ST_X(geom) AS lng, ST_Y(geom) AS lat, " +
                        "  recorded_at, speed " +
                        "FROM asset_positions " +
                        "WHERE asset_id = ? " +
                        "  AND recorded_at BETWEEN ?::timestamp AND (?::timestamp + interval '1 day') " +
                        "ORDER BY recorded_at ASC";
        } else {
            // Default: last 24 hours of GPS history
            pointsSql = "SELECT ST_X(geom) AS lng, ST_Y(geom) AS lat, " +
                        "  recorded_at, speed " +
                        "FROM asset_positions " +
                        "WHERE asset_id = ? " +
                        "  AND recorded_at >= NOW() - INTERVAL '24 hours' " +
                        "ORDER BY recorded_at ASC";
        }

        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(pointsSql)) {

            ps.setInt(1, assetId);
            if (start != null && !start.isEmpty() && end != null && !end.isEmpty()) {
                ps.setString(2, start);
                ps.setString(3, end);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    coords.add(new double[]{ rs.getDouble("lng"), rs.getDouble("lat") });
                    Timestamp ts = rs.getTimestamp("recorded_at");
                    timestamps.add(ts != null ? ts.toInstant().toString() : "");
                    speeds.add(rs.getDouble("speed"));
                }
            }
        } catch (SQLException e) {
            log.error("TrackServlet point query failed: {}", e.getMessage(), e);
            result.addProperty("status", "error");
            result.addProperty("message", e.getMessage());
            resp.setStatus(500);
            resp.getWriter().write(result.toString());
            return;
        }

        if (coords.isEmpty()) {
            result.addProperty("status", "success");
            result.add("data", new JsonObject());
            resp.getWriter().write(result.toString());
            return;
        }

        // ── Step 2: build aggregate stats ────────────────────────────────────
        double totalDist = 0;
        double totalSpeed = 0;
        for (int i = 1; i < coords.size(); i++) {
            double[] a = coords.get(i - 1);
            double[] b = coords.get(i);
            totalDist += haversineMetres(a[1], a[0], b[1], b[0]);
        }
        for (Double sp : speeds) totalSpeed += sp;
        double avgSpeed = speeds.isEmpty() ? 0 : totalSpeed / speeds.size();

        // ── Step 3: assemble GeoJSON Feature ─────────────────────────────────
        JsonObject geometry = new JsonObject();
        geometry.addProperty("type", "LineString");
        JsonArray coordsArr = new JsonArray();
        for (double[] p : coords) {
            JsonArray pt = new JsonArray();
            pt.add(p[0]); // lng
            pt.add(p[1]); // lat
            coordsArr.add(pt);
        }
        geometry.add("coordinates", coordsArr);

        JsonArray tsArr = new JsonArray();
        for (String t : timestamps) tsArr.add(t);

        JsonArray spArr = new JsonArray();
        for (Double sp : speeds) spArr.add(sp);

        JsonObject properties = new JsonObject();
        properties.addProperty("assetId",    assetId);
        properties.addProperty("pointCount", coords.size());
        properties.addProperty("distanceM",  totalDist);
        properties.addProperty("avgSpeed",   avgSpeed);
        properties.addProperty("startedAt",  timestamps.isEmpty() ? "" : timestamps.get(0));
        properties.addProperty("endedAt",    timestamps.isEmpty() ? "" : timestamps.get(timestamps.size() - 1));
        properties.add("timestamps", tsArr);
        properties.add("speeds",     spArr);

        JsonObject feature = new JsonObject();
        feature.addProperty("type", "Feature");
        feature.add("geometry", geometry);
        feature.add("properties", properties);

        result.addProperty("status", "success");
        result.add("data", feature);
        resp.getWriter().write(result.toString());
    }

    /** Simple Haversine distance in metres between two lat/lng points. */
    private static double haversineMetres(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}

