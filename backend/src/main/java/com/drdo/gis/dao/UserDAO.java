package com.drdo.gis.dao;

import com.drdo.gis.config.DatabaseConfig;
import com.drdo.gis.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private static final Logger log = LoggerFactory.getLogger(UserDAO.class);

    public List<User> findAll() {
        String sql = "SELECT id, username, password_hash, full_name, role, email, is_active, last_login, created_at "
                   + "FROM users ORDER BY id";
        List<User> users = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) users.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findAll failed: {}", e.getMessage(), e);
        }
        return users;
    }

    public User findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, full_name, role, email, is_active, last_login, created_at "
                   + "FROM users WHERE username = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            log.error("findByUsername failed: {}", e.getMessage(), e);
        }
        return null;
    }

    public boolean updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = NOW() WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("updateLastLogin failed: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean updateRole(int userId, String newRole) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newRole);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("updateRole failed: {}", e.getMessage(), e);
            return false;
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(rs.getString("role"));
        u.setEmail(rs.getString("email"));
        u.setActive(rs.getBoolean("is_active"));
        u.setLastLogin(rs.getTimestamp("last_login"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}

