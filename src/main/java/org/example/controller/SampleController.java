package org.example.controller;

import org.example.model.Sample;
import org.example.repository.SampleRepository;
import org.example.view.SampleInputView;
import org.example.view.SampleOutputView;

import java.util.List;

public class SampleController {
    private final SampleRepository repository;
    private final SampleInputView in;
    private final SampleOutputView out;

    public SampleController(SampleRepository repository, SampleInputView in, SampleOutputView out) {
        this.repository = repository;
        this.in = in;
        this.out = out;
    }

    public void run() {
        while (true) {
            out.showMenu();
            switch (in.readLine()) {
                case "1" -> register();
                case "2" -> listAll();
                case "3" -> search();
                case "0" -> { return; }
                default  -> out.showError("올바른 번호를 입력하세요.");
            }
        }
    }

    private void register() {
        out.promptId();
        String id = in.readId();
        if (repository.findById(id).isPresent()) {
            out.showError("이미 존재하는 시료 ID입니다: " + id);
            return;
        }
        out.promptName();
        String name = in.readName();
        out.promptAvgProductionTime();
        double avgTime = in.readAvgProductionTime();
        out.promptYieldRate();
        double yieldRate = in.readYieldRate();
        out.promptStock();
        int stock = in.readStock();

        Sample sample = new Sample(id, name, avgTime, yieldRate, stock);
        out.showRegisterConfirm(sample);

        if ("Y".equalsIgnoreCase(in.readLine())) {
            repository.save(sample);
            out.showRegisterSuccess(sample);
        }
    }

    private void listAll() {
        out.showSampleList(repository.findAll());
    }

    private void search() {
        out.promptSearchKeyword();
        String keyword = in.readSearchKeyword();
        List<Sample> results = repository.findAll().stream()
                .filter(s -> s.getName().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
        if (results.isEmpty()) {
            out.showNotFound();
        } else {
            out.showSampleList(results);
        }
    }
}
