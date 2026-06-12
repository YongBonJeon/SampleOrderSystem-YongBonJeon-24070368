package org.example.model;

public class Sample {
    private String id;
    private String name;
    private double avgProductionTime;
    private double yieldRate;
    private int stock;

    public Sample(String id, String name, double avgProductionTime, double yieldRate, int stock) {
        this.id = id;
        this.name = name;
        this.avgProductionTime = avgProductionTime;
        this.yieldRate = yieldRate;
        this.stock = stock;
    }

    public String getId()                { return id; }
    public String getName()              { return name; }
    public double getAvgProductionTime() { return avgProductionTime; }
    public double getYieldRate()         { return yieldRate; }
    public int getStock()                { return stock; }
    public void setStock(int stock)      { this.stock = stock; }
}
