package org.example.model;

import java.time.LocalDateTime;

public class ProductionJob {

    private final String orderId;
    private final String sampleId;
    private final int shortage;
    private final double avgProductionTime;
    private final int actualQty;
    private final double totalTime;
    private LocalDateTime startedAt;

    public ProductionJob(String orderId, String sampleId, int shortage,
                         double yieldRate, double avgProductionTime) {
        this.orderId = orderId;
        this.sampleId = sampleId;
        this.shortage = shortage;
        this.avgProductionTime = avgProductionTime;
        this.actualQty = (int) Math.ceil(shortage / (yieldRate * 0.9));
        this.totalTime = avgProductionTime * this.actualQty;
    }

    // 재시작 후 큐 복원 시 사용 — actualQty를 직접 지정
    public ProductionJob(String orderId, String sampleId, int actualQty, double avgProductionTime) {
        this.orderId = orderId;
        this.sampleId = sampleId;
        this.shortage = 0;
        this.avgProductionTime = avgProductionTime;
        this.actualQty = actualQty;
        this.totalTime = avgProductionTime * actualQty;
    }

    public String getOrderId()            { return orderId; }
    public String getSampleId()           { return sampleId; }
    public int getShortage()              { return shortage; }
    public double getAvgProductionTime()  { return avgProductionTime; }
    public int getActualQty()             { return actualQty; }
    public double getTotalTime()          { return totalTime; }
    public LocalDateTime getStartedAt()   { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
}
