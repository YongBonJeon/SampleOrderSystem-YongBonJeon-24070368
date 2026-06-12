package org.example.controller;

import org.example.repository.OrderRepository;
import org.example.repository.SampleRepository;
import org.example.view.InputView;
import org.example.view.OutputView;

public class MainController {
    private final SampleRepository sampleRepository;
    private final OrderRepository orderRepository;
    private final InputView in;
    private final OutputView out;

    public MainController(SampleRepository sampleRepository, OrderRepository orderRepository,
                          InputView in, OutputView out) {
        this.sampleRepository = sampleRepository;
        this.orderRepository = orderRepository;
        this.in = in;
        this.out = out;
    }

    public void run() {
        while (true) {
            out.showMainMenu();
            String input = in.readLine();
            switch (input) {
                case "1" -> out.showNotImplemented();
                case "2" -> out.showNotImplemented();
                case "3" -> out.showNotImplemented();
                case "4" -> out.showNotImplemented();
                case "5" -> out.showNotImplemented();
                case "6" -> out.showNotImplemented();
                case "0" -> { out.println("종료합니다."); return; }
                default  -> out.showError("올바른 번호를 입력하세요.");
            }
        }
    }
}
