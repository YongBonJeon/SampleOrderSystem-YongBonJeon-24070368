package org.example;

import org.example.controller.MainController;
import org.example.controller.SampleController;
import org.example.repository.impl.InMemoryOrderRepository;
import org.example.repository.impl.InMemorySampleRepository;
import org.example.view.InputView;
import org.example.view.OutputView;
import org.example.view.SampleInputView;
import org.example.view.SampleOutputView;

import java.util.Scanner;

public class Application {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        InMemorySampleRepository sampleRepository = new InMemorySampleRepository();

        SampleController sampleController = new SampleController(
                sampleRepository,
                new SampleInputView(scanner),
                new SampleOutputView(System.out)
        );

        new MainController(
                sampleRepository,
                new InMemoryOrderRepository(),
                new InputView(scanner),
                new OutputView(),
                sampleController
        ).run();
    }
}
