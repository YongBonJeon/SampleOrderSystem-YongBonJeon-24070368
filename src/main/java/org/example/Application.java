package org.example;

import org.example.controller.ApprovalController;
import org.example.controller.ProductionQueueRestorer;
import org.example.controller.MainController;
import org.example.controller.MonitoringController;
import org.example.controller.OrderController;
import org.example.controller.ProductionLineController;
import org.example.controller.ReleaseController;
import org.example.controller.SampleController;
import org.example.persistence.PersistenceType;
import org.example.persistence.RepositoryFactory;
import org.example.queue.ProductionQueue;
import org.example.repository.OrderRepository;
import org.example.repository.SampleRepository;
import org.example.util.OrderIdGenerator;
import org.example.view.ApprovalInputView;
import org.example.view.ApprovalOutputView;
import org.example.view.InputView;
import org.example.view.MonitoringInputView;
import org.example.view.MonitoringOutputView;
import org.example.view.OrderInputView;
import org.example.view.OrderOutputView;
import org.example.view.OutputView;
import org.example.view.ProductionLineOutputView;
import org.example.view.ReleaseInputView;
import org.example.view.ReleaseOutputView;
import org.example.view.SampleInputView;
import org.example.view.SampleOutputView;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Application {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        PersistenceType type = PersistenceType.fromString(
                System.getProperty("persistence", "FILE"));
        RepositoryFactory factory = new RepositoryFactory(type);
        SampleRepository sampleRepository = factory.createSampleRepository();
        OrderRepository orderRepository = factory.createOrderRepository();
        ProductionQueue productionQueue = new ProductionQueue();
        ProductionQueueRestorer.restore(sampleRepository, orderRepository, productionQueue);

        SampleController sampleController = new SampleController(
                sampleRepository,
                new SampleInputView(scanner),
                new SampleOutputView(System.out)
        );

        List<String> existingOrderIds = orderRepository.findAll().stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
        OrderController orderController = new OrderController(
                sampleRepository,
                orderRepository,
                new OrderIdGenerator(LocalDate.now(), existingOrderIds),
                new OrderInputView(scanner),
                new OrderOutputView(System.out)
        );

        ApprovalController approvalController = new ApprovalController(
                sampleRepository,
                orderRepository,
                productionQueue,
                new ApprovalInputView(scanner),
                new ApprovalOutputView(System.out)
        );

        ProductionLineController productionLineController = new ProductionLineController(
                sampleRepository,
                orderRepository,
                productionQueue,
                new ProductionLineOutputView(System.out)
        );

        MonitoringController monitoringController = new MonitoringController(
                sampleRepository,
                orderRepository,
                new MonitoringInputView(scanner),
                new MonitoringOutputView(System.out)
        );

        ReleaseController releaseController = new ReleaseController(
                sampleRepository,
                orderRepository,
                new ReleaseInputView(scanner),
                new ReleaseOutputView(System.out)
        );

        new MainController(
                sampleRepository,
                orderRepository,
                new InputView(scanner),
                new OutputView(),
                sampleController,
                orderController,
                approvalController,
                productionLineController,
                monitoringController,
                releaseController
        ).run();
    }

}
