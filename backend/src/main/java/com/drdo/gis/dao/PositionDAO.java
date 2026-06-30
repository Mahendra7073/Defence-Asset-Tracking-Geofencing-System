package com.drdo.gis.dao;

import com.drdo.gis.config.DatabaseConfig;
import com.drdo.gis.model.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PositionDAO {

    private static final Logger log = LoggerFactory.getLogger(PositionDAO.class);

    /**
     * Insert a new GPS position into asset_positions.
     * Uses ST_SetSRID(ST_Point(lng, lat), 4326) to build the PostGIS geometry.
     */
    public int insert(Position p) {
        String sql = "INSERT INTO asset_positions (asset_id, geom, speed, heading, altitude, accuracy) "
                   + "VALUES (?, ST_SetSRID(ST_Point(?, ?), 4326), ?, ?, ?, ?) RETURNING id";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, p.getAssetId());
            ps.setDouble(2, p.getLongitude());
            ps.setDouble(3, p.getLatitude());
            ps.setDouble(4, p.getSpeed());
            ps.setDouble(5, p.getHeading());
            ps.setDouble(6, p.getAltitude());
            ps.setDouble(7, p.getAccuracy());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("insert failed: {}", e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Get the latest position for each active asset (one row per asset).
     * This is the primary query for the live tracking map.
     */
    public List<Position> findLatestAll() {
        String sql = "SELECT DISTINCT ON (ap.asset_id) "
                   + "  ap.id, ap.asset_id, ST_X(ap.geom) AS lng, ST_Y(ap.geom) AS lat, "
                   + "  ap.speed, ap.heading, ap.altitude, ap.accuracy, ap.recorded_at, ap.received_at, "
                   + "  a.asset_name, a.asset_type, a.asset_code "
                   + "FROM asset_positions ap "
                   + "JOIN assets a ON a.id = ap.asset_id "
                   + "WHERE a.status = 'active' "
                   + "ORDER BY ap.asset_id, ap.recorded_at DESC";
        List<Position> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findLatestAll failed: {}", e.getMessage(), e);
        }
        return list;
    }

    /**
     * Get the latest position for a specific asset.
     */
    public Position findLatestByAsset(int assetId) {
        String sql = "SELECT ap.id, ap.asset_id, ST_X(ap.geom) AS lng, ST_Y(ap.geom) AS lat, "
                   + "  ap.speed, ap.heading, ap.altitude, ap.accuracy, ap.recorded_at, ap.received_at, "
                   + "  a.asset_name, a.asset_type, a.asset_code "
                   + "FROM asset_positions ap "
                   + "JOIN assets a ON a.id = ap.asset_id "
                   + "WHERE ap.asset_id = ? ORDER BY ap.recorded_at DESC LIMIT 1";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, assetId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            log.error("findLatestByAsset failed: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Get position history for an asset within a date range.
     */
    public List<Position> findByAssetAndDateRange(int assetId, Timestamp start, Timestamp end) {
        String sql = "SELECT ap.id, ap.asset_id, ST_X(ap.geom) AS lng, ST_Y(ap.geom) AS lat, "
                   + "  ap.speed, ap.heading, ap.altitude, ap.accuracy, ap.recorded_at, ap.received_at, "
                   + "  a.asset_name, a.asset_type, a.asset_code "
                   + "FROM asset_positions ap "
                   + "JOIN assets a ON a.id = ap.asset_id "
                   + "WHERE ap.asset_id = ? AND ap.recorded_at BETWEEN ? AND ? "
                   + "ORDER BY ap.recorded_at ASC";
        List<Position> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, assetId);
            ps.setTimestamp(2, start);
            ps.setTimestamp(3, end);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("findByAssetAndDateRange failed: {}", e.getMessage(), e);
        }
        return list;
    }

    private Position mapRow(ResultSet rs) throws SQLException {
        Position p = new Position();
        p.setId(rs.getInt("id"));
        p.setAssetId(rs.getInt("asset_id"));
        p.setLongitude(rs.getDouble("lng"));
        p.setLatitude(rs.getDouble("lat"));
        p.setSpeed(rs.getDouble("speed"));
        p.setHeading(rs.getDouble("heading"));
        p.setAltitude(rs.getDouble("altitude"));
        p.setAccuracy(rs.getDouble("accuracy"));
        p.setRecordedAt(rs.getTimestamp("recorded_at"));
        p.setReceivedAt(rs.getTimestamp("received_at"));
        p.setAssetName(rs.getString("asset_name"));
        p.setAssetType(rs.getString("asset_type"));
        p.setAssetCode(rs.getString("asset_code"));
        return p;
    }
}
