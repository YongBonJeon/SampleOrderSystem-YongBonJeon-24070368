package org.example.model;

import java.time.LocalDateTime;

public class Order implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private String orderId;
    private String sampleId;
    private String customerName;
    private int quantity;
    private OrderStatus status;
    private LocalDateTime orderedAt;

    public Order(String orderId, String sampleId, String customerName, int quantity, OrderStatus status) {
        this.orderId = orderId;
        this.sampleId = sampleId;
        this.customerName = customerName;
        this.quantity = quantity;
        this.status = status;
        this.orderedAt = LocalDateTime.now();
    }

    public String getOrderId()        { return orderId; }
    public String getSampleId()       { return sampleId; }
    public String getCustomerName()   { return customerName; }
    public int getQuantity()          { return quantity; }
    public OrderStatus getStatus()    { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public LocalDateTime getOrderedAt() { return orderedAt; }
}
