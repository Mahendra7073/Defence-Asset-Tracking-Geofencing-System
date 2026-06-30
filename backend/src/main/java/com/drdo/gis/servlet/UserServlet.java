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
}
