package org.example.controller;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.repository.OrderRepository;
import org.example.view.ReleaseInputView;
import org.example.view.ReleaseOutputView;

import java.time.LocalDateTime;
import java.util.List;

public class ReleaseController {

    private final OrderRepository orderRepository;
    private final ReleaseInputView in;
    private final ReleaseOutputView out;

    public ReleaseController(OrderRepository orderRepository, ReleaseInputView in, ReleaseOutputView out) {
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
        out.showReleased(order, LocalDateTime.now());
    }
}
