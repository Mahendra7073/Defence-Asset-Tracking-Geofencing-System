package com.drdo.gis.servlet;

import com.drdo.gis.dao.AlertDAO;
import com.drdo.gis.model.Alert;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = "/api/alerts/*")
public class AlertServlet extends HttpServlet {

    private final AlertDAO dao = new AlertDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String unack = req.getParameter("unack");
        String severity = req.getParameter("severity");
        String period = req.getParameter("period");
        int limit = 50;
        try { limit = Integer.parseInt(req.getParameter("limit")); } catch (Exception ignored) {}

        List<Alert> alerts;
        if ("true".equals(unack)) {
            alerts = dao.findUnacknowledged();
        } else {
            alerts = dao.findAll(limit, severity, period);
        }
        JsonArray arr = gson.toJsonTree(alerts).getAsJsonArray();
        JsonObject out = new JsonObject();
        out.addProperty("status", "success");
        out.add("data", arr);
        resp.getWriter().write(out.toString());
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2) {
            resp.setStatus(400);
            JsonObject e = new JsonObject();
            e.addProperty("status", "error");
            e.addProperty("message", "Use PUT /api/alerts/{id}/acknowledge or /api/alerts/acknowledge-all");
            resp.getWriter().write(e.toString());
            return;
        }

        int userId = (int) req.getSession().getAttribute("userId");

        if ("/acknowledge-all".equals(pathInfo)) {
            int updated = dao.acknowledgeAll(userId);
            JsonObject out = new JsonObject();
            out.addProperty("status", updated >= 0 ? "success" : "error");
            out.addProperty("message", updated >= 0 ? "Alerts acknowledged: " + updated : "Failed to acknowledge alerts");
            resp.setStatus(updated >= 0 ? 200 : 500);
            resp.getWriter().write(out.toString());
            return;
        }

        if (!pathInfo.contains("/acknowledge")) {
            resp.setStatus(400);
            JsonObject e = new JsonObject();
            e.addProperty("status", "error");
            e.addProperty("message", "Use PUT /api/alerts/{id}/acknowledge or /api/alerts/acknowledge-all");
            resp.getWriter().write(e.toString());
            return;
        }

        // Extract alertId from path: /123/acknowledge
        String[] parts = pathInfo.substring(1).split("/");
        int alertId = Integer.parseInt(parts[0]);

        boolean ok = dao.acknowledge(alertId, userId);
        JsonObject out = new JsonObject();
        out.addProperty("status", ok ? "success" : "error");
        out.addProperty("message", ok ? "Alert acknowledged" : "Failed to acknowledge");
        resp.setStatus(ok ? 200 : 500);
        resp.getWriter().write(out.toString());
    }
}
