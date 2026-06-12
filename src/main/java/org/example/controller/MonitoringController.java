package org.example.controller;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.model.Sample;
import org.example.model.StockLevel;
import org.example.repository.OrderRepository;
import org.example.repository.SampleRepository;
import org.example.view.MonitoringInputView;
import org.example.view.MonitoringOutputView;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MonitoringController {

    private final SampleRepository sampleRepository;
    private final OrderRepository orderRepository;
    private final MonitoringInputView in;
    private final MonitoringOutputView out;

    public MonitoringController(SampleRepository sampleRepository, OrderRepository orderRepository,
                                MonitoringInputView in, MonitoringOutputView out) {
        this.sampleRepository = sampleRepository;
        this.orderRepository = orderRepository;
        this.in = in;
        this.out = out;
    }

    public void run() {
        while (true) {
            out.showSubMenu();
            String selection = in.readSubMenuSelection();
            switch (selection) {
                case "1" -> showOrderCounts();
                case "2" -> showStockStatus();
                case "0" -> { return; }
            }
        }
    }

    private void showOrderCounts() {
        Map<OrderStatus, Long> counts = Map.of(
                OrderStatus.RESERVED,  (long) orderRepository.findByStatus(OrderStatus.RESERVED).size(),
                OrderStatus.PRODUCING, (long) orderRepository.findByStatus(OrderStatus.PRODUCING).size(),
                OrderStatus.CONFIRMED, (long) orderRepository.findByStatus(OrderStatus.CONFIRMED).size(),
                OrderStatus.RELEASE,   (long) orderRepository.findByStatus(OrderStatus.RELEASE).size()
        );
        out.showOrderCounts(counts);
    }

    private void showStockStatus() {
        List<Sample> samples = sampleRepository.findAll();
        List<Order> reservedOrders = orderRepository.findByStatus(OrderStatus.RESERVED);

        Map<String, Integer> reservedQtyById = reservedOrders.stream()
                .collect(Collectors.groupingBy(Order::getSampleId,
                        Collectors.summingInt(Order::getQuantity)));

        Map<String, StockLevel> levels = samples.stream()
                .collect(Collectors.toMap(Sample::getId,
                        s -> assessLevel(s, reservedQtyById.getOrDefault(s.getId(), 0))));

        Map<String, Double> remainingRates = samples.stream()
                .collect(Collectors.toMap(Sample::getId, s -> {
                    int reserved = reservedQtyById.getOrDefault(s.getId(), 0);
                    int total = s.getStock() + reserved;
                    return total == 0 ? 0.0 : (double) s.getStock() / total * 100.0;
                }));

        out.showStockStatus(samples, levels, remainingRates);
    }

    private StockLevel assessLevel(Sample sample, int reservedQty) {
        if (sample.getStock() == 0) return StockLevel.DEPLETED;
        if (sample.getStock() < reservedQty) return StockLevel.SHORTAGE;
        return StockLevel.SURPLUS;
    }
}
