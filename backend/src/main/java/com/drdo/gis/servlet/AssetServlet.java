package com.drdo.gis.servlet;

import com.drdo.gis.dao.AssetDAO;
import com.drdo.gis.model.Asset;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = "/api/assets/*")
public class AssetServlet extends HttpServlet {

    private final AssetDAO dao = new AssetDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            // GET /api/assets/{id}
            int id = Integer.parseInt(pathInfo.substring(1));
            Asset a = dao.findById(id);
            if (a == null) {
                writeJson(resp, 404, "error", "Asset not found", null);
            } else {
                writeJson(resp, 200, "success", null, gson.toJsonTree(a).getAsJsonObject());
            }
            return;
        }

        // GET /api/assets?type=vehicle
        String typeFilter = req.getParameter("type");
        List<Asset> assets;
        if (typeFilter != null && !typeFilter.isEmpty() && !"all".equals(typeFilter)) {
            assets = dao.findByType(typeFilter);
        } else {
            assets = dao.findAll();
        }
        JsonArray arr = gson.toJsonTree(assets).getAsJsonArray();
        writeJson(resp, 200, "success", null, arr);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        Asset a = readBody(req, Asset.class);
        if (a == null || a.getAssetName() == null) {
            writeJson(resp, 400, "error", "Invalid asset data", null);
            return;
        }
        int id = dao.create(a);
        if (id > 0) {
            JsonObject data = new JsonObject();
            data.addProperty("id", id);
            writeJson(resp, 201, "success", "Asset created", data);
        } else {
            writeJson(resp, 500, "error", "Failed to create asset", null);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2) {
            writeJson(resp, 400, "error", "Asset ID required", null);
            return;
        }
        int id = Integer.parseInt(pathInfo.substring(1));
        Asset a = readBody(req, Asset.class);
        if (a == null) { writeJson(resp, 400, "error", "Invalid data", null); return; }
        
        Asset existing = dao.findById(id);
        if (existing == null) {
            writeJson(resp, 404, "error", "Asset not found", null);
            return;
        }
        
        if (a.getAssetName() != null) existing.setAssetName(a.getAssetName());
        if (a.getAssetType() != null) existing.setAssetType(a.getAssetType());
        if (a.getAssetCode() != null) existing.setAssetCode(a.getAssetCode());
        if (a.getDescription() != null) existing.setDescription(a.getDescription());
        if (a.getStatus() != null) existing.setStatus(a.getStatus());
        
        boolean ok = dao.update(existing);
        writeJson(resp, ok ? 200 : 500, ok ? "success" : "error", ok ? "Asset updated" : "Update failed", null);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2) {
            writeJson(resp, 400, "error", "Asset ID required", null);
            return;
        }
        int id = Integer.parseInt(pathInfo.substring(1));
        boolean ok = dao.delete(id);
        writeJson(resp, ok ? 200 : 404, ok ? "success" : "error", ok ? "Asset deleted" : "Asset not found", null);
    }

    private <T> T readBody(HttpServletRequest req, Class<T> cls) {
        try (BufferedReader r = req.getReader()) {
            return gson.fromJson(r, cls);
        } catch (Exception e) {
            return null;
        }
    }

    private void writeJson(HttpServletResponse resp, int status, String s, String msg, Object data) throws IOException {
        resp.setStatus(status);
        JsonObject out = new JsonObject();
        out.addProperty("status", s);
        if (msg != null) out.addProperty("message", msg);
        if (data instanceof JsonObject) out.add("data", (JsonObject) data);
        else if (data instanceof JsonArray) out.add("data", (JsonArray) data);
        resp.getWriter().write(out.toString());
    }
}
