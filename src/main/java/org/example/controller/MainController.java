package org.example.controller;

import org.example.model.OrderStatus;
import org.example.repository.OrderRepository;
import org.example.repository.SampleRepository;
import org.example.view.InputView;
import org.example.view.OutputView;

import java.time.LocalDateTime;

public class MainController {
    private final SampleRepository sampleRepository;
    private final OrderRepository orderRepository;
    private final InputView in;
    private final OutputView out;
    private final SampleController sampleController;
    private final OrderController orderController;
    private final ApprovalController approvalController;
    private final ProductionLineController productionLineController;
    private final MonitoringController monitoringController;
    private final ReleaseController releaseController;

    public MainController(SampleRepository sampleRepository, OrderRepository orderRepository,
                          InputView in, OutputView out, SampleController sampleController,
                          OrderController orderController, ApprovalController approvalController,
                          ProductionLineController productionLineController,
                          MonitoringController monitoringController,
                          ReleaseController releaseController) {
        this.sampleRepository = sampleRepository;
        this.orderRepository = orderRepository;
        this.in = in;
        this.out = out;
        this.sampleController = sampleController;
        this.orderController = orderController;
        this.approvalController = approvalController;
        this.productionLineController = productionLineController;
        this.monitoringController = monitoringController;
        this.releaseController = releaseController;
    }

    public void run() {
        while (true) {
            int sampleCount = sampleRepository.findAll().size();
            int totalStock = sampleRepository.findAll().stream().mapToInt(s -> s.getStock()).sum();
            int orderCount = (int) orderRepository.findAll().stream()
                    .filter(o -> o.getStatus() != OrderStatus.REJECTED).count();
            int producingCount = orderRepository.findByStatus(OrderStatus.PRODUCING).size();
            out.showMainMenu(sampleCount, totalStock, orderCount, producingCount, LocalDateTime.now());
            String input = in.readLine();
            switch (input) {
                case "1" -> sampleController.run();
                case "2" -> orderController.run();
                case "3" -> approvalController.run();
                case "4" -> monitoringController.run();
                case "5" -> productionLineController.run();
                case "6" -> releaseController.run();
                case "0" -> { out.println("종료합니다."); return; }
                default  -> out.showError("올바른 번호를 입력하세요.");
            }
        }
    }
}
