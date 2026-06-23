package com.defence.gis;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public static Connection getConnection() throws Exception {

        String url = "jdbc:postgresql://localhost:5432/defence_gis;"; 
        String user = "postgres";
        String password = "Your_POSTGRESQL_PASSWORD_HERE"; 
        
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(url, user, password);
    }
}