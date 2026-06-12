package org.example.controller;

import org.example.repository.impl.InMemoryOrderRepository;
import org.example.repository.impl.InMemorySampleRepository;
import org.example.view.InputView;
import org.example.view.OutputView;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class MainControllerTest {

    private MainController controllerWith(String input) {
        InputView inputView = new InputView(new ByteArrayInputStream(input.getBytes()));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputView outputView = new OutputView(new PrintStream(baos));
        return new MainController(
                new InMemorySampleRepository(),
                new InMemoryOrderRepository(),
                inputView,
                outputView
        );
    }

    private String outputOf(String input) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputView inputView = new InputView(new ByteArrayInputStream(input.getBytes()));
        OutputView outputView = new OutputView(new PrintStream(baos));
        new MainController(
                new InMemorySampleRepository(),
                new InMemoryOrderRepository(),
                inputView,
                outputView
        ).run();
        return baos.toString();
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
        String output = outputOf("1\n0\n");

        assertTrue(output.contains("미구현") || output.contains("준비"),
                "미구현 메뉴는 안내 메시지를 출력해야 한다");
    }
}
