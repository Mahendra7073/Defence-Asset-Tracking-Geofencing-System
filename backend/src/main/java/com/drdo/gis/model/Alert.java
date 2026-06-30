package com.drdo.gis.model;

import java.sql.Timestamp;

public class Alert {
    private int id;
    private int assetId;
    private int zoneId;
    private String alertType;
    private String severity;
    private double lat;
    private double lng;
    private boolean acknowledged;
    private int ackBy;
    private Timestamp ackAt;
    private Timestamp triggeredAt;

    private String assetName;
    private String zoneName;

    public Alert() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAssetId() { return assetId; }
    public void setAssetId(int assetId) { this.assetId = assetId; }

    public int getZoneId() { return zoneId; }
    public void setZoneId(int zoneId) { this.zoneId = zoneId; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public boolean isAcknowledged() { return acknowledged; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }

    public int getAckBy() { return ackBy; }
    public void setAckBy(int ackBy) { this.ackBy = ackBy; }

    public Timestamp getAckAt() { return ackAt; }
    public void setAckAt(Timestamp ackAt) { this.ackAt = ackAt; }

    public Timestamp getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(Timestamp triggeredAt) { this.triggeredAt = triggeredAt; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }
}
