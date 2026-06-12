package org.example.controller;

import org.example.model.Sample;
import org.example.repository.impl.InMemorySampleRepository;
import org.example.view.SampleInputView;
import org.example.view.SampleOutputView;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class SampleControllerTest {

    private record Context(SampleController controller, InMemorySampleRepository repo, ByteArrayOutputStream output) {}

    private Context buildContext(String input) {
        return buildContext(input, new InMemorySampleRepository());
    }

    private Context buildContext(String input, InMemorySampleRepository repo) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SampleInputView inputView = new SampleInputView(scanner);
        SampleOutputView outputView = new SampleOutputView(new PrintStream(baos, true, StandardCharsets.UTF_8));
        return new Context(new SampleController(repo, inputView, outputView), repo, baos);
    }

    @Test
    void register_withValidInput_savesSample() {
        // menu=1, id, name, avgTime, yield, stock, confirm=Y, exit=0
        String input = "1\nS-001\nSilicon-Wafer\n0.5\n0.9\n100\nY\n0\n";
        Context ctx = buildContext(input);

        ctx.controller().run();

        assertTrue(ctx.repo().findById("S-001").isPresent());
        assertEquals("Silicon-Wafer", ctx.repo().findById("S-001").get().getName());
    }

    @Test
    void register_withConfirmNo_doesNotSaveSample() {
        String input = "1\nS-001\nSilicon-Wafer\n0.5\n0.9\n100\nN\n0\n";
        Context ctx = buildContext(input);

        ctx.controller().run();

        assertFalse(ctx.repo().findById("S-001").isPresent());
    }

    @Test
    void register_withDuplicateId_showsError() {
        InMemorySampleRepository repo = new InMemorySampleRepository();
        repo.save(new Sample("S-001", "Existing", 0.5, 0.9, 100));

        // 중복 ID 입력 후 바로 서브메뉴로 복귀, exit=0
        String input = "1\nS-001\n0\n";
        Context ctx = buildContext(input, repo);

        ctx.controller().run();

        assertEquals(1, ctx.repo().findAll().size()); // 추가 저장 없음
        assertTrue(ctx.output().toString(StandardCharsets.UTF_8).contains("ERROR"));
    }

    @Test
    void listAll_showsAllSamples() {
        InMemorySampleRepository repo = new InMemorySampleRepository();
        repo.save(new Sample("S-001", "Silicon-Wafer", 0.5, 0.9, 100));
        repo.save(new Sample("S-002", "GaN-Epitaxial", 0.3, 0.78, 50));

        String input = "2\n0\n";
        Context ctx = buildContext(input, repo);

        ctx.controller().run();

        String output = ctx.output().toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("S-001"));
        assertTrue(output.contains("S-002"));
    }

    @Test
    void listAll_whenEmpty_showsEmptyMessage() {
        String input = "2\n0\n";
        Context ctx = buildContext(input);

        ctx.controller().run();

        String output = ctx.output().toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("없") || output.contains("empty") || output.contains("0"));
    }

    @Test
    void search_withMatchingKeyword_showsSamples() {
        InMemorySampleRepository repo = new InMemorySampleRepository();
        repo.save(new Sample("S-001", "Silicon-Wafer", 0.5, 0.9, 100));
        repo.save(new Sample("S-002", "GaN-Epitaxial", 0.3, 0.78, 50));

        String input = "3\nSilicon\n0\n";
        Context ctx = buildContext(input, repo);

        ctx.controller().run();

        String output = ctx.output().toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("S-001"));
        assertFalse(output.contains("S-002"));
    }

    @Test
    void search_withNoMatch_showsNotFoundMessage() {
        InMemorySampleRepository repo = new InMemorySampleRepository();
        repo.save(new Sample("S-001", "Silicon-Wafer", 0.5, 0.9, 100));

        String input = "3\nNoMatch\n0\n";
        Context ctx = buildContext(input, repo);

        ctx.controller().run();

        String output = ctx.output().toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("없") || output.contains("not found") || output.contains("결과"));
    }
}
