package com.drdo.gis.servlet;

import com.drdo.gis.dao.AlertDAO;
import com.drdo.gis.dao.AssetDAO;
import com.drdo.gis.dao.GeofenceDAO;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(urlPatterns = "/api/dashboard")
public class DashboardServlet extends HttpServlet {

    private final AssetDAO assetDAO = new AssetDAO();
    private final GeofenceDAO geofenceDAO = new GeofenceDAO();
    private final AlertDAO alertDAO = new AlertDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        JsonObject data = new JsonObject();
        data.addProperty("totalAssets", assetDAO.countAll());
        data.addProperty("activeAssets", assetDAO.countByStatus("active"));
        data.addProperty("totalZones", geofenceDAO.countActive());
        data.addProperty("unacknowledgedAlerts", alertDAO.countUnacknowledged());

        JsonObject out = new JsonObject();
        out.addProperty("status", "success");
        out.add("data", data);
        resp.getWriter().write(out.toString());
    }
}
