package org.example.controller;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.model.ProductionJob;
import org.example.model.Sample;
import org.example.queue.ProductionQueue;
import org.example.repository.OrderRepository;
import org.example.repository.SampleRepository;
import org.example.view.ApprovalInputView;
import org.example.view.ApprovalOutputView;

import java.util.List;

public class ApprovalController {

    private final SampleRepository sampleRepository;
    private final OrderRepository orderRepository;
    private final ProductionQueue productionQueue;
    private final ApprovalInputView in;
    private final ApprovalOutputView out;

    public ApprovalController(SampleRepository sampleRepository, OrderRepository orderRepository,
                              ProductionQueue productionQueue, ApprovalInputView in, ApprovalOutputView out) {
        this.sampleRepository = sampleRepository;
        this.orderRepository = orderRepository;
        this.productionQueue = productionQueue;
        this.in = in;
        this.out = out;
    }

    public void run() {
        List<Order> reserved = orderRepository.findByStatus(OrderStatus.RESERVED);
        if (reserved.isEmpty()) {
            out.showNoReservedOrders();
            return;
        }

        out.showReservedList(reserved);
        String orderId = in.readOrderId();

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            out.showOrderNotFound(orderId);
            return;
        }
        if (order.getStatus() != OrderStatus.RESERVED) {
            out.showOrderNotReserved(orderId);
            return;
        }

        String decision = in.readDecision();
        if ("Y".equals(decision)) {
            approve(order);
        } else {
            reject(order);
        }
    }

    private void approve(Order order) {
        Sample sample = sampleRepository.findById(order.getSampleId()).get();
        if (sample.getStock() >= order.getQuantity()) {
            sample.setStock(sample.getStock() - order.getQuantity());
            sampleRepository.update(sample);
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.update(order);
            out.showConfirmed(order);
        } else {
            int shortage = order.getQuantity() - sample.getStock();
            ProductionJob job = new ProductionJob(
                    order.getOrderId(), sample.getId(),
                    shortage, sample.getYieldRate(), sample.getAvgProductionTime());
            productionQueue.enqueue(job);
            order.setStatus(OrderStatus.PRODUCING);
            orderRepository.update(order);
            out.showProducing(order, job);
        }
    }

    private void reject(Order order) {
        order.setStatus(OrderStatus.REJECTED);
        orderRepository.update(order);
        out.showRejected(order);
    }
}
