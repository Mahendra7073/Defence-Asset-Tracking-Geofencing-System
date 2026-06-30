package com.drdo.gis.servlet;

import com.drdo.gis.dao.GeofenceDAO;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(urlPatterns = "/api/geofences/*")
public class GeofenceServlet extends HttpServlet {

    private final GeofenceDAO dao = new GeofenceDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            String idStr = pathInfo.substring(1); // strip leading "/"
            try {
                int id = Integer.parseInt(idStr);
                String detailsJson = dao.findDetailsAsJson(id);
                if (detailsJson != null) {
                    resp.getWriter().write(detailsJson);
                    return;
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"status\":\"error\",\"message\":\"Geofence zone not found\"}");
                    return;
                }
            } catch (NumberFormatException e) {
                // Not a valid integer ID, fall back to default behavior or error
            }
        }

        String geoJson = dao.findAllAsGeoJson();
        JsonObject out = new JsonObject();
        out.addProperty("status", "success");
        out.add("data", com.google.gson.JsonParser.parseString(geoJson));
        resp.getWriter().write(out.toString());
    }
}
