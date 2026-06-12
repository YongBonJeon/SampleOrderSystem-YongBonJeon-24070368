package org.example.controller;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.model.ProductionJob;
import org.example.model.Sample;
import org.example.queue.ProductionQueue;
import org.example.repository.OrderRepository;
import org.example.repository.SampleRepository;
import org.example.view.ProductionLineOutputView;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Supplier;

public class ProductionLineController {

    private final SampleRepository sampleRepository;
    private final OrderRepository orderRepository;
    private final ProductionQueue productionQueue;
    private final ProductionLineOutputView out;
    private final Supplier<LocalDateTime> clock;

    public ProductionLineController(SampleRepository sampleRepository, OrderRepository orderRepository,
                                    ProductionQueue productionQueue, ProductionLineOutputView out) {
        this(sampleRepository, orderRepository, productionQueue, out, LocalDateTime::now);
    }

    public ProductionLineController(SampleRepository sampleRepository, OrderRepository orderRepository,
                                    ProductionQueue productionQueue, ProductionLineOutputView out,
                                    Supplier<LocalDateTime> clock) {
        this.sampleRepository = sampleRepository;
        this.orderRepository = orderRepository;
        this.productionQueue = productionQueue;
        this.out = out;
        this.clock = clock;
    }

    public void run() {
        if (productionQueue.isEmpty()) {
            out.showNoJobInProgress();
            return;
        }

        ProductionJob current = productionQueue.peek().get();
        LocalDateTime now = clock.get();

        if (current.getStartedAt() != null) {
            double elapsedMinutes = Duration.between(current.getStartedAt(), now).toSeconds() / 60.0;
            if (elapsedMinutes >= current.getTotalTime()) {
                complete();
                return;
            }
        }

        out.showProductionLine(current, productionQueue.getWaiting(), now);
    }

    private void complete() {
        ProductionJob job = productionQueue.dequeue();

        Sample sample = sampleRepository.findById(job.getSampleId()).get();
        int newStock = sample.getStock() + job.getActualQty();
        sample.setStock(newStock);
        sampleRepository.update(sample);

        Order order = orderRepository.findById(job.getOrderId()).get();
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.update(order);

        out.showCompleted(job, newStock);
    }
}
