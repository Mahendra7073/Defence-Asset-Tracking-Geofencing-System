package com.drdo.gis.servlet;

import com.drdo.gis.dao.UserDAO;
import com.drdo.gis.model.User;
import com.drdo.gis.util.PasswordUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet(urlPatterns = {"/api/auth/login", "/api/auth/logout", "/api/auth/session"})
public class LoginServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(LoginServlet.class);
    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getServletPath() + (req.getPathInfo() != null ? req.getPathInfo() : "");

        if (path.endsWith("/logout")) {
            HttpSession session = req.getSession(false);
            if (session != null) session.invalidate();
            writeJson(resp, 200, "success", "Logged out", null);
            return;
        }

        // POST /api/auth/login
        JsonObject body = readJsonBody(req);
        if (body == null || !body.has("username") || !body.has("password")) {
            writeJson(resp, 400, "error", "Username and password required", null);
            return;
        }

        String username = body.get("username").getAsString().trim();
        String password = body.get("password").getAsString();

        User user = userDAO.findByUsername(username);
        if (user == null || !user.isActive()) {
            writeJson(resp, 401, "error", "Invalid username or password", null);
            return;
        }

        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            writeJson(resp, 401, "error", "Invalid username or password", null);
            return;
        }

        userDAO.updateLastLogin(user.getId());

        HttpSession session = req.getSession(true);
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole());
        session.setMaxInactiveInterval(30 * 60);

        JsonObject data = new JsonObject();
        data.addProperty("userId", user.getId());
        data.addProperty("username", user.getUsername());
        data.addProperty("fullName", user.getFullName());
        data.addProperty("role", user.getRole());

        writeJson(resp, 200, "success", "Login successful", data);
        log.info("User '{}' logged in", username);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            writeJson(resp, 401, "error", "No active session", null);
            return;
        }
        JsonObject data = new JsonObject();
        data.addProperty("userId", (int) session.getAttribute("userId"));
        data.addProperty("username", (String) session.getAttribute("username"));
        data.addProperty("role", (String) session.getAttribute("role"));
        data.addProperty("isActive", true);
        writeJson(resp, 200, "success", "Session active", data);
    }

    private JsonObject readJsonBody(HttpServletRequest req) {
        try (BufferedReader r = req.getReader()) {
            return gson.fromJson(r, JsonObject.class);
        } catch (Exception e) {
            return null;
        }
    }

    private void writeJson(HttpServletResponse resp, int status, String statusStr, String message, JsonObject data) throws IOException {
        resp.setStatus(status);
        JsonObject out = new JsonObject();
        out.addProperty("status", statusStr);
        out.addProperty("message", message);
        if (data != null) out.add("data", data);
        resp.getWriter().write(out.toString());
    }
}
