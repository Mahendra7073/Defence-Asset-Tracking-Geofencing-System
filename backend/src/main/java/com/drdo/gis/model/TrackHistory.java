package com.drdo.gis.model;

import java.sql.Timestamp;

public class TrackHistory {
    private int id;
    private int assetId;
    private Timestamp startedAt;
    private Timestamp endedAt;
    private double distanceM;
    private double avgSpeed;
    private int pointCount;

    public TrackHistory() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAssetId() { return assetId; }
    public void setAssetId(int assetId) { this.assetId = assetId; }

    public Timestamp getStartedAt() { return startedAt; }
    public void setStartedAt(Timestamp startedAt) { this.startedAt = startedAt; }

    public Timestamp getEndedAt() { return endedAt; }
    public void setEndedAt(Timestamp endedAt) { this.endedAt = endedAt; }

    public double getDistanceM() { return distanceM; }
    public void setDistanceM(double distanceM) { this.distanceM = distanceM; }

    public double getAvgSpeed() { return avgSpeed; }
    public void setAvgSpeed(double avgSpeed) { this.avgSpeed = avgSpeed; }

    public int getPointCount() { return pointCount; }
    public void setPointCount(int pointCount) { this.pointCount = pointCount; }
}
