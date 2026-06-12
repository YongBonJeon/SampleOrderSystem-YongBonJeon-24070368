package org.example.controller;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.model.Sample;
import org.example.repository.OrderRepository;
import org.example.repository.SampleRepository;
import org.example.util.OrderIdGenerator;
import org.example.view.OrderInputView;
import org.example.view.OrderOutputView;

public class OrderController {

    private final SampleRepository sampleRepository;
    private final OrderRepository orderRepository;
    private final OrderIdGenerator idGenerator;
    private final OrderInputView in;
    private final OrderOutputView out;

    public OrderController(SampleRepository sampleRepository, OrderRepository orderRepository,
                           OrderIdGenerator idGenerator, OrderInputView in, OrderOutputView out) {
        this.sampleRepository = sampleRepository;
        this.orderRepository = orderRepository;
        this.idGenerator = idGenerator;
        this.in = in;
        this.out = out;
    }

    public void run() {
        placeOrder();
    }

    private void placeOrder() {
        String sampleId = in.readSampleId();
        Sample sample = sampleRepository.findById(sampleId).orElse(null);
        if (sample == null) {
            out.showSampleNotFound(sampleId);
            return;
        }

        String customerName = in.readCustomerName();
        int quantity = in.readQuantity();

        out.showOrderConfirm(sample, customerName, quantity);
        String confirm = in.readConfirm();

        if ("Y".equals(confirm)) {
            Order order = new Order(idGenerator.generate(), sampleId, customerName, quantity, OrderStatus.RESERVED);
            orderRepository.save(order);
            out.showOrderComplete(order);
        } else {
            out.showOrderCancelled();
        }
    }
}
