package org.example.persistence;

import org.example.repository.SampleRepository;
import org.example.repository.impl.DatabaseSampleRepository;
import org.example.repository.impl.FileSampleRepository;
import org.example.repository.impl.JsonSampleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RepositoryFactoryAllTypesTest {

    @Test
    @DisplayName("FILE 타입으로 생성한 SampleRepository는 FileSampleRepository다")
    void createSampleRepository_withFileType(@TempDir Path tempDir) {
        SampleRepository repo = new RepositoryFactory(PersistenceType.FILE, tempDir).createSampleRepository();
        assertInstanceOf(FileSampleRepository.class, repo);
    }

    @Test
    @DisplayName("JSON 타입으로 생성한 SampleRepository는 JsonSampleRepository다")
    void createSampleRepository_withJsonType(@TempDir Path tempDir) {
        SampleRepository repo = new RepositoryFactory(PersistenceType.JSON, tempDir).createSampleRepository();
        assertInstanceOf(JsonSampleRepository.class, repo);
    }

    @Test
    @DisplayName("DATABASE 타입으로 생성한 SampleRepository는 DatabaseSampleRepository다")
    void createSampleRepository_withDatabaseType(@TempDir Path tempDir) {
        SampleRepository repo = new RepositoryFactory(PersistenceType.DATABASE, tempDir).createSampleRepository();
        assertInstanceOf(DatabaseSampleRepository.class, repo);
    }
}
