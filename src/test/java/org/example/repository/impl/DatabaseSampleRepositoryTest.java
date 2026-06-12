package org.example.repository.impl;

import org.example.model.Sample;
import org.example.persistence.DatabaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseSampleRepositoryTest {

    private Connection conn;
    private DatabaseSampleRepository repo;

    @BeforeEach
    void setUp() throws Exception {
        conn = DatabaseConfig.openMemory("sample_test_" + System.nanoTime());
        DatabaseConfig.initSchema(conn);
        repo = new DatabaseSampleRepository(conn);
    }

    @Test
    @DisplayName("H2 인메모리 DB에서 save/findById/update/deleteById가 올바르게 동작한다")
    void databaseSampleRepository_fullCrud() {
        Sample sample = new Sample("S-001", "SiC 파워기판", 0.8, 0.92, 100);

        repo.save(sample);
        Sample found = repo.findById("S-001").orElseThrow();
        assertEquals("SiC 파워기판", found.getName());
        assertEquals(100, found.getStock());

        found.setStock(200);
        repo.update(found);
        assertEquals(200, repo.findById("S-001").orElseThrow().getStock());

        repo.deleteById("S-001");
        assertTrue(repo.findById("S-001").isEmpty());
    }

    @Test
    @DisplayName("findAll 은 저장된 모든 시료를 반환한다")
    void databaseSampleRepository_findAll() {
        repo.save(new Sample("S-001", "시료A", 0.5, 0.9, 50));
        repo.save(new Sample("S-002", "시료B", 1.0, 0.8, 30));

        List<Sample> all = repo.findAll();

        assertEquals(2, all.size());
    }
}
