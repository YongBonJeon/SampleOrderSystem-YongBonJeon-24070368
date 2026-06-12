package org.example.controller;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.model.ProductionJob;
import org.example.queue.ProductionQueue;
import org.example.repository.OrderRepository;
import org.example.repository.SampleRepository;

import java.util.Comparator;
import java.util.List;

public class ProductionQueueRestorer {

    public static void restore(SampleRepository sampleRepo,
                               OrderRepository orderRepo,
                               ProductionQueue queue) {
        List<Order> producing = orderRepo.findByStatus(OrderStatus.PRODUCING);
        producing.stream()
                .sorted(Comparator.comparing(Order::getOrderedAt))
                .forEach(order -> sampleRepo.findById(order.getSampleId()).ifPresent(sample -> {
                    ProductionJob job = new ProductionJob(
                            order.getOrderId(), order.getSampleId(),
                            order.getActualQty(), sample.getAvgProductionTime());
                    queue.enqueue(job);
                }));
    }
}
