package org.example.repository;

import org.example.model.Sample;
import org.example.repository.impl.InMemorySampleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemorySampleRepositoryTest {

    private SampleRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemorySampleRepository();
    }

    @Test
    void save_thenFindById_returnsStoredSample() {
        Sample sample = new Sample("S-001", "실리콘 웨이퍼", 0.5, 0.92, 100);

        repository.save(sample);
        Optional<Sample> result = repository.findById("S-001");

        assertTrue(result.isPresent());
        assertEquals("S-001", result.get().getId());
        assertEquals("실리콘 웨이퍼", result.get().getName());
    }

    @Test
    void findAll_returnsAllSavedSamples() {
        repository.save(new Sample("S-001", "실리콘 웨이퍼", 0.5, 0.92, 100));
        repository.save(new Sample("S-002", "GaN 에피택셜", 0.3, 0.78, 50));

        List<Sample> result = repository.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void findById_withNonExistentId_returnsEmpty() {
        Optional<Sample> result = repository.findById("S-999");

        assertFalse(result.isPresent());
    }

    @Test
    void deleteById_thenFindById_returnsEmpty() {
        repository.save(new Sample("S-001", "실리콘 웨이퍼", 0.5, 0.92, 100));

        repository.deleteById("S-001");
        Optional<Sample> result = repository.findById("S-001");

        assertFalse(result.isPresent());
    }

    @Test
    void update_thenFindById_returnsUpdatedSample() {
        repository.save(new Sample("S-001", "실리콘 웨이퍼", 0.5, 0.92, 100));

        Sample updated = new Sample("S-001", "실리콘 웨이퍼", 0.5, 0.92, 200);
        repository.update(updated);

        Optional<Sample> result = repository.findById("S-001");
        assertTrue(result.isPresent());
        assertEquals(200, result.get().getStock());
    }
}
