package org.example.controller;

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
                stubApprovalController()
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
                stubApprovalController()
        ).run();
        return baos.toString(StandardCharsets.UTF_8);
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

    @Test
    void run_withMenuInput_printsNotImplementedMessage() {
        String output = outputOf("4\n0\n"); // 메뉴 4(모니터링)은 아직 미구현

        assertTrue(output.contains("미구현") || output.contains("준비"),
                "미구현 메뉴는 안내 메시지를 출력해야 한다");
    }
}
