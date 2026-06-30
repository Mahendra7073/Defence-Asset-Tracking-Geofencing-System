package com.drdo.gis.dao;

import com.drdo.gis.config.DatabaseConfig;
import com.drdo.gis.model.TrackHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrackDAO {

    private static final Logger log = LoggerFactory.getLogger(TrackDAO.class);



    public List<TrackHistory> findByAsset(int assetId) {
        String sql = "SELECT id, asset_id, started_at, ended_at, distance_m, avg_speed, point_count "
                   + "FROM track_history WHERE asset_id = ? ORDER BY started_at DESC";
        List<TrackHistory> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, assetId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TrackHistory t = new TrackHistory();
                    t.setId(rs.getInt("id"));
                    t.setAssetId(rs.getInt("asset_id"));
                    t.setStartedAt(rs.getTimestamp("started_at"));
                    t.setEndedAt(rs.getTimestamp("ended_at"));
                    t.setDistanceM(rs.getDouble("distance_m"));
                    t.setAvgSpeed(rs.getDouble("avg_speed"));
                    t.setPointCount(rs.getInt("point_count"));
                    list.add(t);
                }
            }
        } catch (SQLException e) {
            log.error("findByAsset failed: {}", e.getMessage(), e);
        }
        return list;
    }
}
