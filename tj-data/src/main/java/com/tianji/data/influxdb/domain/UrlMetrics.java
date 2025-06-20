package com.tianji.data.influxdb.domain;

public class UrlMetrics {
    private long totalVisits;       // 总访问量
    private long successVisits;     // 成功访问量
    private long failedVisits;      // 失败访问量
    
    // Getter和Setter方法
    public long getTotalVisits() { return totalVisits; }
    public void setTotalVisits(long totalVisits) { this.totalVisits = totalVisits; }
    public long getSuccessVisits() { return successVisits; }
    public void setSuccessVisits(long successVisits) { this.successVisits = successVisits; }
    public long getFailedVisits() { return failedVisits; }
    public void setFailedVisits(long failedVisits) { this.failedVisits = failedVisits; }
    
    @Override
    public String toString() {
        return "UrlMetrics{" +
                "totalVisits=" + totalVisits +
                ", successVisits=" + successVisits +
                ", failedVisits=" + failedVisits +
                '}';
    }
}