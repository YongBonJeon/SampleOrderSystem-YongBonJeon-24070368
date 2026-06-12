package org.example;

import org.example.controller.ApprovalController;
import org.example.controller.MainController;
import org.example.controller.OrderController;
import org.example.controller.SampleController;
import org.example.queue.ProductionQueue;
import org.example.repository.impl.InMemoryOrderRepository;
import org.example.repository.impl.InMemorySampleRepository;
import org.example.util.OrderIdGenerator;
import org.example.view.ApprovalInputView;
import org.example.view.ApprovalOutputView;
import org.example.view.InputView;
import org.example.view.OrderInputView;
import org.example.view.OrderOutputView;
import org.example.view.OutputView;
import org.example.view.SampleInputView;
import org.example.view.SampleOutputView;

import java.util.Scanner;

public class Application {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        InMemorySampleRepository sampleRepository = new InMemorySampleRepository();
        InMemoryOrderRepository orderRepository = new InMemoryOrderRepository();
        ProductionQueue productionQueue = new ProductionQueue();

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

        new MainController(
                sampleRepository,
                orderRepository,
                new InputView(scanner),
                new OutputView(),
                sampleController,
                orderController,
                approvalController
        ).run();
    }
}
