package com.drdo.gis.servlet;

import com.drdo.gis.dao.UserDAO;
import com.drdo.gis.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/users/*")
public class UserServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        List<User> users = userDAO.findAll();

        // Remove password hashes from output
        users.forEach(u -> u.setPasswordHash(null));

        JsonObject json = new JsonObject();
        json.addProperty("status", "success");
        json.add("data", gson.toJsonTree(users));
        resp.getWriter().write(json.toString());
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2) {
            writeJson(resp, 400, "error", "User ID required", null);
            return;
        }
        int id = Integer.parseInt(pathInfo.substring(1));
        
        User u;
        try (java.io.BufferedReader r = req.getReader()) {
            u = gson.fromJson(r, User.class);
        } catch (Exception e) {
            writeJson(resp, 400, "error", "Invalid JSON format", null);
            return;
        }
        
        if (u == null || u.getRole() == null) {
            writeJson(resp, 400, "error", "Role is required", null);
            return;
        }
        
        boolean ok = userDAO.updateRole(id, u.getRole());
        writeJson(resp, ok ? 200 : 500, ok ? "success" : "error", ok ? "User role updated" : "Update failed", null);
    }

    private void writeJson(HttpServletResponse resp, int statusCode, String status, String message, Object data) throws IOException {
        resp.setStatus(statusCode);
        JsonObject json = new JsonObject();
        json.addProperty("status", status);
        if (message != null) json.addProperty("message", message);
        if (data != null) json.add("data", gson.toJsonTree(data));
        resp.getWriter().write(json.toString());
    }
}
