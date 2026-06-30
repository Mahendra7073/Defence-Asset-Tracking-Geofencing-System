package com.drdo.gis.servlet;

import com.drdo.gis.config.DatabaseConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

/**
 * TrackServlet — returns route history as GeoJSON LineString.
 * GET /api/tracks?assetId=1&start=2026-06-01&end=2026-06-22
 */
@WebServlet("/api/tracks")
public class TrackServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        JsonObject result = new JsonObject();

        String assetIdStr = req.getParameter("assetId");
        String start = req.getParameter("start");
        String end = req.getParameter("end");

        if (assetIdStr == null || assetIdStr.isEmpty()) {
            result.addProperty("status", "error");
            result.addProperty("message", "assetId is required");
            resp.setStatus(400);
            resp.getWriter().write(result.toString());
            return;
        }

        int assetId = Integer.parseInt(assetIdStr);

        // Build GeoJSON LineString from asset positions using PostGIS
        String sql = "SELECT json_build_object("
                   + "  'type', 'Feature',"
                   + "  'geometry', ST_AsGeoJSON(ST_MakeLine(geom ORDER BY recorded_at))::json,"
                   + "  'properties', json_build_object("
                   + "    'assetId', ?,"
                   + "    'pointCount', COUNT(*),"
                   + "    'distanceM', ST_Length(ST_MakeLine(geom ORDER BY recorded_at)::geography),"
                   + "    'avgSpeed', AVG(speed),"
                   + "    'startedAt', MIN(recorded_at)::text,"
                   + "    'endedAt', MAX(recorded_at)::text"
                   + "  )"
                   + ") AS track_geojson "
                   + "FROM asset_positions WHERE asset_id = ?";

        // Append date range if provided
        if (start != null && !start.isEmpty() && end != null && !end.isEmpty()) {
            sql += " AND recorded_at BETWEEN ?::timestamp AND (?::timestamp + interval '1 day')";
        }

        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, assetId);
            ps.setInt(2, assetId);
            if (start != null && !start.isEmpty() && end != null && !end.isEmpty()) {
                ps.setString(3, start);
                ps.setString(4, end);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String geojson = rs.getString("track_geojson");
                    result.addProperty("status", "success");
                    result.add("data", gson.fromJson(geojson, JsonObject.class));
                } else {
                    result.addProperty("status", "success");
                    result.add("data", new JsonObject());
                }
            }
        } catch (SQLException e) {
            result.addProperty("status", "error");
            result.addProperty("message", e.getMessage());
            resp.setStatus(500);
        }

        resp.getWriter().write(result.toString());
    }
}
