package org.example.view;

import org.example.model.Sample;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SampleOutputViewTest {

    private SampleOutputView buildView(ByteArrayOutputStream baos) {
        return new SampleOutputView(new PrintStream(baos, true, StandardCharsets.UTF_8));
    }

    @Test
    void showSampleList_containsIdAndName() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SampleOutputView view = buildView(baos);
        List<Sample> samples = List.of(
                new Sample("S-001", "Silicon-Wafer", 0.5, 0.92, 480),
                new Sample("S-002", "GaN-Epitaxial", 0.3, 0.78, 220)
        );

        view.showSampleList(samples);

        String output = baos.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("S-001"));
        assertTrue(output.contains("Silicon-Wafer"));
        assertTrue(output.contains("S-002"));
        assertTrue(output.contains("480"));
    }

    @Test
    void showRegisterSuccess_containsSampleIdAndName() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SampleOutputView view = buildView(baos);
        Sample sample = new Sample("S-003", "SiC-PowerBoard", 0.8, 0.92, 0);

        view.showRegisterSuccess(sample);

        String output = baos.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("S-003"));
        assertTrue(output.contains("SiC-PowerBoard"));
    }

    @Test
    void showNotFound_containsNotFoundText() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SampleOutputView view = buildView(baos);

        view.showNotFound();

        String output = baos.toString(StandardCharsets.UTF_8);
        assertFalse(output.isBlank());
    }
}
