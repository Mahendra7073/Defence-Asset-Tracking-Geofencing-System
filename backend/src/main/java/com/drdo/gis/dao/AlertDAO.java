package com.drdo.gis.dao;

import com.drdo.gis.config.DatabaseConfig;
import com.drdo.gis.model.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlertDAO {

    private static final Logger log = LoggerFactory.getLogger(AlertDAO.class);

    public int insert(Alert a) {
        String sql = "INSERT INTO alerts (asset_id, zone_id, alert_type, severity, lat, lng) "
                   + "VALUES (?,?,?,?,?,?) RETURNING id";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, a.getAssetId());
            ps.setInt(2, a.getZoneId());
            ps.setString(3, a.getAlertType());
            ps.setString(4, a.getSeverity());
            ps.setDouble(5, a.getLat());
            ps.setDouble(6, a.getLng());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("insert failed: {}", e.getMessage(), e);
        }
        return -1;
    }

    public List<Alert> findAll(int limit) {
        return findAll(limit, null);
    }

    public List<Alert> findAll(int limit, String severity) {
        return findAll(limit, severity, null);
    }

    public List<Alert> findAll(int limit, String severity, String period) {
        String sql = "SELECT al.id, al.asset_id, al.zone_id, al.alert_type, al.severity, "
                   + "al.lat, al.lng, al.acknowledged, al.ack_by, al.ack_at, al.triggered_at, "
                   + "a.asset_name, gz.zone_name "
                   + "FROM alerts al "
                   + "JOIN assets a ON a.id = al.asset_id "
                   + "JOIN geofence_zones gz ON gz.id = al.zone_id "
                   + "WHERE 1=1 ";
        if (severity != null && !severity.isBlank()) {
            sql += "AND al.severity = ? ";
        }
        if (period != null && !period.isBlank() && !"all".equals(period)) {
            if ("week".equals(period)) {
                sql += "AND al.triggered_at >= NOW() - INTERVAL '7 days' ";
            } else if ("month".equals(period)) {
                sql += "AND al.triggered_at >= NOW() - INTERVAL '30 days' ";
            }
        }
        sql += "ORDER BY al.triggered_at DESC";
        if (limit > 0) {
            sql += " LIMIT ?";
        }
        return query(sql, limit, severity);
    }

    public List<Alert> findUnacknowledged() {
        String sql = "SELECT al.id, al.asset_id, al.zone_id, al.alert_type, al.severity, "
                   + "al.lat, al.lng, al.acknowledged, al.ack_by, al.ack_at, al.triggered_at, "
                   + "a.asset_name, gz.zone_name "
                   + "FROM alerts al "
                   + "JOIN assets a ON a.id = al.asset_id "
                   + "JOIN geofence_zones gz ON gz.id = al.zone_id "
                   + "WHERE al.acknowledged = FALSE ORDER BY al.triggered_at DESC";
        return query(sql, -1, null);
    }

    public int countUnacknowledged() {
        String sql = "SELECT COUNT(*) FROM alerts WHERE acknowledged = FALSE";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log.error("countUnacknowledged failed: {}", e.getMessage(), e);
        }
        return 0;
    }

    public boolean acknowledge(int alertId, int userId) {
        String sql = "UPDATE alerts SET acknowledged = TRUE, ack_by = ?, ack_at = NOW() WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, alertId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("acknowledge failed: {}", e.getMessage(), e);
            return false;
        }
    }

    public int acknowledgeAll(int userId) {
        String sql = "UPDATE alerts SET acknowledged = TRUE, ack_by = ?, ack_at = NOW() WHERE acknowledged = FALSE";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            log.error("acknowledgeAll failed: {}", e.getMessage(), e);
            return -1;
        }
    }

    public boolean existsUnackForAssetZone(int assetId, int zoneId, String alertType) {
        String sql = "SELECT 1 FROM alerts WHERE asset_id = ? AND zone_id = ? AND alert_type = ? AND acknowledged = FALSE LIMIT 1";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, assetId);
            ps.setInt(2, zoneId);
            ps.setString(3, alertType);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.error("existsUnackForAssetZone failed: {}", e.getMessage(), e);
            return false;
        }
    }

    private List<Alert> query(String sql, int limit, String severity) {
        List<Alert> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int index = 1;
            if (severity != null && !severity.isBlank()) {
                ps.setString(index++, severity);
            }
            if (limit > 0) {
                ps.setInt(index, limit);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("query failed: {}", e.getMessage(), e);
        }
        return list;
    }

    private Alert mapRow(ResultSet rs) throws SQLException {
        Alert a = new Alert();
        a.setId(rs.getInt("id"));
        a.setAssetId(rs.getInt("asset_id"));
        a.setZoneId(rs.getInt("zone_id"));
        a.setAlertType(rs.getString("alert_type"));
        a.setSeverity(rs.getString("severity"));
        a.setLat(rs.getDouble("lat"));
        a.setLng(rs.getDouble("lng"));
        a.setAcknowledged(rs.getBoolean("acknowledged"));
        a.setAckBy(rs.getInt("ack_by"));
        a.setAckAt(rs.getTimestamp("ack_at"));
        a.setTriggeredAt(rs.getTimestamp("triggered_at"));
        a.setAssetName(rs.getString("asset_name"));
        a.setZoneName(rs.getString("zone_name"));
        return a;
    }
}
