package com.drdo.gis.servlet;

import com.drdo.gis.dao.PositionDAO;
import com.drdo.gis.model.Alert;
import com.drdo.gis.model.Position;
import com.drdo.gis.service.GeofenceService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = "/api/positions/*")
public class PositionServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(PositionServlet.class);
    private final PositionDAO dao = new PositionDAO();
    private final GeofenceService geofenceService = new GeofenceService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String pathInfo = req.getPathInfo();

        // GET /api/positions/latest
        if (pathInfo != null && pathInfo.equals("/latest")) {
            List<Position> positions = dao.findLatestAll();
            JsonObject geoJson = toGeoJsonFeatureCollection(positions);
            JsonObject out = new JsonObject();
            out.addProperty("status", "success");
            out.add("data", geoJson);
            resp.getWriter().write(out.toString());
            return;
        }

        // GET /api/positions/{assetId}
        if (pathInfo != null && pathInfo.length() > 1) {
            int assetId = Integer.parseInt(pathInfo.substring(1));
            Position p = dao.findLatestByAsset(assetId);
            JsonObject out = new JsonObject();
            out.addProperty("status", "success");
            if (p != null) out.add("data", gson.toJsonTree(p));
            resp.getWriter().write(out.toString());
            return;
        }

        resp.setStatus(400);
        JsonObject err = new JsonObject();
        err.addProperty("status", "error");
        err.addProperty("message", "Specify /latest or /{assetId}");
        resp.getWriter().write(err.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        Position p;
        try (BufferedReader r = req.getReader()) {
            p = gson.fromJson(r, Position.class);
        } catch (Exception e) {
            resp.setStatus(400);
            JsonObject err = new JsonObject();
            err.addProperty("status", "error");
            err.addProperty("message", "Invalid JSON body");
            resp.getWriter().write(err.toString());
            return;
        }

        if (p == null || p.getAssetId() <= 0) {
            resp.setStatus(400);
            JsonObject err = new JsonObject();
            err.addProperty("status", "error");
            err.addProperty("message", "assetId is required");
            resp.getWriter().write(err.toString());
            return;
        }

        int posId = dao.insert(p);
        if (posId < 0) {
            resp.setStatus(500);
            JsonObject err = new JsonObject();
            err.addProperty("status", "error");
            err.addProperty("message", "Failed to insert position");
            resp.getWriter().write(err.toString());
            return;
        }

        // Geofence breach detection
        List<Alert> alerts = geofenceService.checkBreach(p.getLongitude(), p.getLatitude(), p.getAssetId());

        resp.setStatus(201);
        JsonObject out = new JsonObject();
        out.addProperty("status", "success");
        JsonObject data = new JsonObject();
        data.addProperty("positionId", posId);
        if (!alerts.isEmpty()) {
            data.add("alerts", gson.toJsonTree(alerts));
        }
        out.add("data", data);
        resp.getWriter().write(out.toString());
        log.info("Position ingested: asset={}, pos={}, breaches={}", p.getAssetId(), posId, alerts.size());
    }

    /**
     * Convert a list of Positions to a GeoJSON FeatureCollection.
     */
    private JsonObject toGeoJsonFeatureCollection(List<Position> positions) {
        JsonObject fc = new JsonObject();
        fc.addProperty("type", "FeatureCollection");
        JsonArray features = new JsonArray();
        for (Position p : positions) {
            JsonObject feature = new JsonObject();
            feature.addProperty("type", "Feature");

            JsonObject geometry = new JsonObject();
            geometry.addProperty("type", "Point");
            JsonArray coords = new JsonArray();
            coords.add(p.getLongitude());
            coords.add(p.getLatitude());
            geometry.add("coordinates", coords);
            feature.add("geometry", geometry);

            JsonObject props = new JsonObject();
            props.addProperty("assetId", p.getAssetId());
            props.addProperty("assetName", p.getAssetName());
            props.addProperty("assetType", p.getAssetType());
            props.addProperty("assetCode", p.getAssetCode());
            props.addProperty("speed", p.getSpeed());
            props.addProperty("heading", p.getHeading());
            props.addProperty("altitude", p.getAltitude());
            if (p.getRecordedAt() != null) props.addProperty("recordedAt", p.getRecordedAt().toString());
            feature.add("properties", props);

            features.add(feature);
        }
        fc.add("features", features);
        return fc;
    }
}
