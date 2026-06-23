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

@WebServlet("/getAssets")
public class AssetServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8"); // UTF-8 handle karne ke liye
        PrintWriter out = response.getWriter();
        StringBuilder json = new StringBuilder();

        // UPDATE: Query mein ST_AsGeoJSON(geom) joda hai location fetch karne ke liye
        String query = "SELECT asset_code, asset_name, status, ST_AsGeoJSON(geom) AS location FROM assets";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            json.append("[");
            while (rs.next()) {
                json.append("{");
                json.append("\"id\":\"").append(rs.getInt("asset_code")).append("\",");
                json.append("\"name\":\"").append(rs.getString("asset_name")).append("\",");
                json.append("\"status\":\"").append(rs.getString("status")).append("\",");
                
                
                String locationJson = rs.getString("location"); 
                if (locationJson == null) {
                    locationJson = "null"; 
                }
                json.append("\"location\":").append(locationJson); 
                
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
            return;
        }
    } 
}