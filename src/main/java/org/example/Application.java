package org.example;

import org.example.controller.ApprovalController;
import org.example.controller.MainController;
import org.example.controller.MonitoringController;
import org.example.controller.OrderController;
import org.example.controller.ProductionLineController;
import org.example.controller.ReleaseController;
import org.example.controller.SampleController;
import org.example.model.Sample;
import org.example.queue.ProductionQueue;
import org.example.repository.impl.InMemoryOrderRepository;
import org.example.repository.impl.InMemorySampleRepository;
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

import java.util.Scanner;

public class Application {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        InMemorySampleRepository sampleRepository = new InMemorySampleRepository();
        InMemoryOrderRepository orderRepository = new InMemoryOrderRepository();
        ProductionQueue productionQueue = new ProductionQueue();

        // TODO(Phase 7): 영속성 구현 후 제거
        loadDummySamples(sampleRepository);

        SampleController sampleController = new SampleController(
                sampleRepository,
                new SampleInputView(scanner),
                new SampleOutputView(System.out)
        );

        OrderController orderController = new OrderController(
                sampleRepository,
                orderRepository,
                new OrderIdGenerator(),
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

    // TODO(Phase 7): 영속성 구현 후 제거
    private static void loadDummySamples(InMemorySampleRepository repo) {
        repo.save(new Sample("S-001", "SiC 파워기판-6인치",   0.80, 0.92, 120));
        repo.save(new Sample("S-002", "산화막 웨이퍼-SiO2",   0.60, 0.88,  50));
        repo.save(new Sample("S-003", "GaN 에피택셜-4인치",   1.20, 0.85,  30));
        repo.save(new Sample("S-004", "실리콘 웨이퍼-8인치",  0.45, 0.95, 200));
        repo.save(new Sample("S-005", "SiC 쇼트키 다이오드",  0.30, 0.90,   0));
    }
}
