package org.example.model;

public class ProductionJob {

    private final String orderId;
    private final String sampleId;
    private final int shortage;
    private final int actualQty;
    private final double totalTime;

    public ProductionJob(String orderId, String sampleId, int shortage,
                         double yieldRate, double avgProductionTime) {
        this.orderId = orderId;
        this.sampleId = sampleId;
        this.shortage = shortage;
        this.actualQty = (int) Math.ceil(shortage / (yieldRate * 0.9));
        this.totalTime = avgProductionTime * this.actualQty;
    }

    public String getOrderId()   { return orderId; }
    public String getSampleId()  { return sampleId; }
    public int getShortage()     { return shortage; }
    public int getActualQty()    { return actualQty; }
    public double getTotalTime() { return totalTime; }
}
