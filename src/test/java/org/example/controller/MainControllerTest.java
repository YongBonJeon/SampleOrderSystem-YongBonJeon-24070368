package org.example.controller;

import org.example.model.Order;
import org.example.model.OrderStatus;
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
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class MainControllerTest {

    private SampleController stubSampleController() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("0\n".getBytes(StandardCharsets.UTF_8)));
        return new SampleController(
                new InMemorySampleRepository(),
                new SampleInputView(scanner),
                new SampleOutputView(new PrintStream(new ByteArrayOutputStream()))
        );
    }

    private OrderController stubOrderController() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
        return new OrderController(
                new InMemorySampleRepository(),
                new InMemoryOrderRepository(),
                new OrderIdGenerator(),
                new OrderInputView(scanner),
                new OrderOutputView(new PrintStream(new ByteArrayOutputStream()))
        );
    }

    private ApprovalController stubApprovalController() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
        return new ApprovalController(
                new InMemorySampleRepository(),
                new InMemoryOrderRepository(),
                new ProductionQueue(),
                new ApprovalInputView(scanner),
                new ApprovalOutputView(new PrintStream(new ByteArrayOutputStream()))
        );
    }

    private ProductionLineController stubProductionLineController() {
        return new ProductionLineController(
                new InMemorySampleRepository(),
                new InMemoryOrderRepository(),
                new ProductionQueue(),
                new ProductionLineOutputView(new PrintStream(new ByteArrayOutputStream()))
        );
    }

    private MonitoringController stubMonitoringController() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("0\n".getBytes(StandardCharsets.UTF_8)));
        return new MonitoringController(
                new InMemorySampleRepository(),
                new InMemoryOrderRepository(),
                new MonitoringInputView(scanner),
                new MonitoringOutputView(new PrintStream(new ByteArrayOutputStream()))
        );
    }

    private ReleaseController stubReleaseController() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
        return new ReleaseController(
                new InMemoryOrderRepository(),
                new ReleaseInputView(scanner),
                new ReleaseOutputView(new PrintStream(new ByteArrayOutputStream()))
        );
    }

    private MainController controllerWith(String input) {
        InputView inputView = new InputView(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputView outputView = new OutputView(new PrintStream(baos));
        return new MainController(
                new InMemorySampleRepository(),
                new InMemoryOrderRepository(),
                inputView,
                outputView,
                stubSampleController(),
                stubOrderController(),
                stubApprovalController(),
                stubProductionLineController(),
                stubMonitoringController(),
                stubReleaseController()
        );
    }

    private String outputOf(String input) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputView inputView = new InputView(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        OutputView outputView = new OutputView(new PrintStream(baos));
        new MainController(
                new InMemorySampleRepository(),
                new InMemoryOrderRepository(),
                inputView,
                outputView,
                stubSampleController(),
                stubOrderController(),
                stubApprovalController(),
                stubProductionLineController(),
                stubMonitoringController(),
                stubReleaseController()
        ).run();
        return baos.toString(StandardCharsets.UTF_8);
    }

    @Test
    void showMainMenu_displaysSystemStatus() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        sampleRepo.save(new Sample("S-001", "웨이퍼A", 0.5, 0.9, 10));
        sampleRepo.save(new Sample("S-002", "웨이퍼B", 0.5, 0.9, 20));

        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        orderRepo.save(new Order("ORD-001", "S-001", "고객A", 5, OrderStatus.RESERVED));
        orderRepo.save(new Order("ORD-002", "S-001", "고객B", 5, OrderStatus.RESERVED));
        orderRepo.save(new Order("ORD-003", "S-002", "고객C", 5, OrderStatus.PRODUCING));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputView inputView = new InputView(new ByteArrayInputStream("0\n".getBytes(StandardCharsets.UTF_8)));
        OutputView outputView = new OutputView(new PrintStream(baos));
        new MainController(
                sampleRepo, orderRepo, inputView, outputView,
                stubSampleController(), stubOrderController(), stubApprovalController(),
                stubProductionLineController(), stubMonitoringController(), stubReleaseController()
        ).run();

        String output = baos.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("2종"), "등록 시료 수");
        assertTrue(output.contains("30 ea"), "총 재고");
        assertTrue(output.contains("3건"), "전체 주문 수");
        assertTrue(output.contains("1건 대기"), "생산라인 대기 수");
    }

    @Test
    void run_withZeroInput_exitsWithoutException() {
        assertDoesNotThrow(() -> controllerWith("0\n").run());
    }

    @Test
    void run_withZeroInput_printsExitMessage() {
        String output = outputOf("0\n");

        assertTrue(output.contains("종료"), "종료 메시지가 출력되어야 한다");
    }

    @Test
    void run_withInvalidInput_printsErrorMessage() {
        String output = outputOf("9\n0\n");

        assertTrue(output.contains("ERROR") || output.contains("올바른"),
                "잘못된 입력에 대한 오류 메시지가 출력되어야 한다");
    }
}
