package org.example.repository.impl;

import org.example.model.Sample;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FileSampleRepositoryTest {

    @Test
    @DisplayName("save 후 새 인스턴스로 재조회하면 동일한 시료가 반환된다")
    void fileSampleRepository_survivesRestart(@TempDir Path tempDir) {
        Sample sample = new Sample("S-001", "SiC 파워기판", 0.8, 0.92, 100);
        new FileSampleRepository(tempDir).save(sample);

        List<Sample> result = new FileSampleRepository(tempDir).findAll();

        assertEquals(1, result.size());
        assertEquals("S-001", result.get(0).getId());
        assertEquals("SiC 파워기판", result.get(0).getName());
        assertEquals(100, result.get(0).getStock());
    }

    @Test
    @DisplayName("findById / update / deleteById 가 파일에 올바르게 반영된다")
    void fileSampleRepository_crudOperations(@TempDir Path tempDir) {
        FileSampleRepository repo = new FileSampleRepository(tempDir);
        Sample sample = new Sample("S-001", "SiC 파워기판", 0.8, 0.92, 100);
        repo.save(sample);

        Optional<Sample> found = repo.findById("S-001");
        assertTrue(found.isPresent());

        sample.setStock(200);
        repo.update(sample);
        assertEquals(200, new FileSampleRepository(tempDir).findById("S-001").get().getStock());

        repo.deleteById("S-001");
        assertTrue(new FileSampleRepository(tempDir).findById("S-001").isEmpty());
    }
}
