package com.defence.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.defence.gis.DBConnection;

@WebServlet("/getGeofences")
public class GeofenceServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        StringBuilder json = new StringBuilder();

        
        String query = "SELECT id, zone_name, description, ST_AsGeoJSON(geom) AS boundary FROM geofence_zones";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            json.append("[");
            while (rs.next()) {
                json.append("{");
                
                json.append("\"zone_id\":\"").append(rs.getInt("id")).append("\",");
                json.append("\"zone_name\":\"").append(rs.getString("zone_name")).append("\",");
                json.append("\"description\":\"").append(rs.getString("description")).append("\",");
                
                String boundaryJson = rs.getString("boundary");
                if (boundaryJson == null) {
                    boundaryJson = "null";
                }
                json.append("\"boundary\":").append(boundaryJson);
                
                json.append("},");
            }

            if (json.length() > 1) {
                json.deleteCharAt(json.length() - 1); 
            }
            json.append("]");

            out.print(json.toString());

        } catch (Exception e) {
            response.setContentType("text/plain");
            e.printStackTrace(out);
        }
    }
}