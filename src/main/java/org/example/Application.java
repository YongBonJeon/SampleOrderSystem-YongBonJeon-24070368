package org.example;

import org.example.controller.MainController;
import org.example.repository.impl.InMemoryOrderRepository;
import org.example.repository.impl.InMemorySampleRepository;
import org.example.view.InputView;
import org.example.view.OutputView;

public class Application {
    public static void main(String[] args) {
        new MainController(
                new InMemorySampleRepository(),
                new InMemoryOrderRepository(),
                new InputView(),
                new OutputView()
        ).run();
    }
}
