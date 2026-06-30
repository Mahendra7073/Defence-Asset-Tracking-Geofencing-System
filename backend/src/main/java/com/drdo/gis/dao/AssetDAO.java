package com.drdo.gis.dao;

import com.drdo.gis.config.DatabaseConfig;
import com.drdo.gis.model.Asset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssetDAO {

    private static final Logger log = LoggerFactory.getLogger(AssetDAO.class);

    public List<Asset> findAll() {
        String sql = "SELECT id, asset_name, asset_type, asset_code, description, status, created_at FROM assets ORDER BY id";
        List<Asset> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findAll failed: {}", e.getMessage(), e);
        }
        return list;
    }

    public List<Asset> findByType(String type) {
        String sql = "SELECT id, asset_name, asset_type, asset_code, description, status, created_at FROM assets WHERE asset_type = ? ORDER BY id";
        List<Asset> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("findByType failed: {}", e.getMessage(), e);
        }
        return list;
    }

    public Asset findById(int id) {
        String sql = "SELECT id, asset_name, asset_type, asset_code, description, status, created_at FROM assets WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            log.error("findById failed: {}", e.getMessage(), e);
        }
        return null;
    }

    public int create(Asset a) {
        String sql = "INSERT INTO assets (asset_name, asset_type, asset_code, description, status) VALUES (?,?,?,?,?) RETURNING id";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getAssetName());
            ps.setString(2, a.getAssetType());
            ps.setString(3, a.getAssetCode());
            ps.setString(4, a.getDescription());
            ps.setString(5, a.getStatus() != null ? a.getStatus() : "active");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("create failed: {}", e.getMessage(), e);
        }
        return -1;
    }

    public boolean update(Asset a) {
        String sql = "UPDATE assets SET asset_name=?, asset_type=?, asset_code=?, description=?, status=? WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getAssetName());
            ps.setString(2, a.getAssetType());
            ps.setString(3, a.getAssetCode());
            ps.setString(4, a.getDescription());
            ps.setString(5, a.getStatus());
            ps.setInt(6, a.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("update failed: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM assets WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("delete failed: {}", e.getMessage(), e);
            return false;
        }
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM assets WHERE status = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("countByStatus failed: {}", e.getMessage(), e);
        }
        return 0;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM assets";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log.error("countAll failed: {}", e.getMessage(), e);
        }
        return 0;
    }

    private Asset mapRow(ResultSet rs) throws SQLException {
        Asset a = new Asset();
        a.setId(rs.getInt("id"));
        a.setAssetName(rs.getString("asset_name"));
        a.setAssetType(rs.getString("asset_type"));
        a.setAssetCode(rs.getString("asset_code"));
        a.setDescription(rs.getString("description"));
        a.setStatus(rs.getString("status"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        return a;
    }
}
