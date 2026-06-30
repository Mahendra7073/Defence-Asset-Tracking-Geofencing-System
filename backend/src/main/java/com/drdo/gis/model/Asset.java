package com.drdo.gis.model;

import java.sql.Timestamp;

public class Asset {
    private int id;
    private String assetName;
    private String assetType;
    private String assetCode;
    private String description;
    private String status;
    private Timestamp createdAt;

    public Asset() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public String getAssetType() { return assetType; }
    public void setAssetType(String assetType) { this.assetType = assetType; }

    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Asset{id=" + id + ", name='" + assetName + "', type='" + assetType + "', code='" + assetCode + "'}";
    }
}
