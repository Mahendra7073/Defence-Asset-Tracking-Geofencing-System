package com.defence.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.defence.gis.DBConnection;

@WebServlet("/updateLocation")
public class UpdateLocationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        
        String assetCode = request.getParameter("asset_code");
        String latStr = request.getParameter("latitude");
        String lonStr = request.getParameter("longitude");

        if (assetCode == null || latStr == null || lonStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"status\":\"error\", \"message\":\"Missing parameters!\"}");
            return;
        }

       
        String query = "INSERT INTO asset_positions (asset_code, geom, updated_at) " +
                       "VALUES (?, ST_SetSRID(ST_MakePoint(?, ?), 4326), CURRENT_TIMESTAMP) " +
                       "ON CONFLICT (asset_code) " +
                       "DO UPDATE SET geom = EXCLUDED.geom, updated_at = CURRENT_TIMESTAMP";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            double lon = Double.parseDouble(lonStr);
            double lat = Double.parseDouble(latStr);

            ps.setString(1, assetCode);
            ps.setDouble(2, lon);
            ps.setDouble(3, lat);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                out.print("{\"status\":\"success\", \"message\":\"Location updated successfully!\"}");
            } else {
                out.print("{\"status\":\"error\", \"message\":\"Failed to update location.\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}