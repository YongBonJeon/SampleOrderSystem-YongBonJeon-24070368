package org.example.repository.impl;

import org.example.model.Sample;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonSampleRepositoryTest {

    @Test
    @DisplayName("JSON save 후 새 인스턴스로 재조회하면 동일한 시료가 반환된다")
    void jsonSampleRepository_survivesRestart(@TempDir Path tempDir) {
        Sample sample = new Sample("S-001", "SiC 파워기판", 0.8, 0.92, 100);
        new JsonSampleRepository(tempDir).save(sample);

        List<Sample> result = new JsonSampleRepository(tempDir).findAll();

        assertEquals(1, result.size());
        assertEquals("S-001", result.get(0).getId());
        assertEquals(0.92, result.get(0).getYieldRate(), 1e-9);
        assertEquals(100, result.get(0).getStock());
    }

    @Test
    @DisplayName("findById / update 결과가 JSON 재시작 후에도 유지된다")
    void jsonSampleRepository_updatePersists(@TempDir Path tempDir) {
        JsonSampleRepository repo = new JsonSampleRepository(tempDir);
        repo.save(new Sample("S-001", "SiC 파워기판", 0.8, 0.92, 100));

        Sample loaded = repo.findById("S-001").orElseThrow();
        loaded.setStock(250);
        repo.update(loaded);

        assertEquals(250, new JsonSampleRepository(tempDir).findById("S-001").orElseThrow().getStock());
    }
}
