package org.example.controller;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.model.Sample;
import org.example.repository.OrderRepository;
import org.example.repository.SampleRepository;
import org.example.view.ReleaseInputView;
import org.example.view.ReleaseOutputView;

import java.time.LocalDateTime;
import java.util.List;

public class ReleaseController {

    private final SampleRepository sampleRepository;
    private final OrderRepository orderRepository;
    private final ReleaseInputView in;
    private final ReleaseOutputView out;

    public ReleaseController(SampleRepository sampleRepository, OrderRepository orderRepository,
                             ReleaseInputView in, ReleaseOutputView out) {
        this.sampleRepository = sampleRepository;
        this.orderRepository = orderRepository;
        this.in = in;
        this.out = out;
    }

    public void run() {
        List<Order> confirmed = orderRepository.findByStatus(OrderStatus.CONFIRMED);
        if (confirmed.isEmpty()) {
            out.showNoConfirmedOrders();
            return;
        }

        out.showConfirmedList(confirmed);
        int selection = in.readSelectionNumber();

        if (selection <= 0 || selection > confirmed.size()) return;

        Order order = confirmed.get(selection - 1);
        order.setStatus(OrderStatus.RELEASE);
        orderRepository.update(order);

        // PRODUCING 경로(승인 시 재고 미차감)는 출고 시 차감
        if (order.getActualQty() > 0) {
            sampleRepository.findById(order.getSampleId()).ifPresent(sample -> {
                sample.setStock(sample.getStock() - order.getQuantity());
                sampleRepository.update(sample);
            });
        }

        out.showReleased(order, LocalDateTime.now());
    }
}
