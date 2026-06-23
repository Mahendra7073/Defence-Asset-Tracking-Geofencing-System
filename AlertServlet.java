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

@WebServlet("/getAlerts")
public class AlertServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        StringBuilder json = new StringBuilder();

        // UPDATED QUERY: Humne asset_code ka column tension hi khatam kar diya
        // Ab hum sidhe assets table ki geometry ko check kar rahe hain jo humne step 1 me update ki thi!
        String query = "SELECT a.asset_code, a.asset_name, z.zone_name, " +
                       "CASE WHEN ST_Contains(z.geom, a.geom) THEN 'INSIDE' ELSE 'BREACH' END as status " +
                       "FROM public.assets a " +
                       "CROSS JOIN public.geofence_zones z";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            json.append("[");
            while (rs.next()) {
                json.append("{");
                json.append("\"asset_code\":\"").append(rs.getString("asset_code")).append("\",");
                json.append("\"asset_name\":\"").append(rs.getString("asset_name")).append("\",");
                json.append("\"zone_name\":\"").append(rs.getString("zone_name")).append("\",");
                json.append("\"status\":\"").append(rs.getString("status")).append("\"");
                json.append("},");
            }

            if (json.length() > 1) {
                json.deleteCharAt(json.length() - 1);
            }
            json.append("]");

            out.print(json.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}