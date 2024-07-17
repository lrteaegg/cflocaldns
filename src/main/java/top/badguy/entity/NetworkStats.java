package top.badguy.entity;

import java.util.Date;

public class NetworkStats {

    private Long id;

    private String ipAddress;

    private Float averageLatency;

    private Boolean isUsed;

    private Date createdAt;

    private Date updatedAt;

    // Getters and Setters

    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    protected void onUpdate() {
        updatedAt = new Date();
    }

    // Constructors, getters, and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Float getAverageLatency() {
        return averageLatency;
    }

    public void setAverageLatency(Float averageLatency) {
        this.averageLatency = averageLatency;
    }

    public Boolean getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
