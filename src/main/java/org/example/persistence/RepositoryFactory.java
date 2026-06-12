package org.example.persistence;

import org.example.repository.OrderRepository;
import org.example.repository.SampleRepository;
import org.example.repository.impl.*;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

public class RepositoryFactory {

    private final PersistenceType type;
    private final Path dataDir;
    private Connection connection;

    public RepositoryFactory(PersistenceType type) {
        this(type, Path.of("data"));
    }

    public RepositoryFactory(PersistenceType type, Path dataDir) {
        this.type = type;
        this.dataDir = dataDir;
    }

    public SampleRepository createSampleRepository() {
        return switch (type) {
            case MEMORY   -> new InMemorySampleRepository();
            case FILE     -> new FileSampleRepository(dataDir);
            case JSON     -> new JsonSampleRepository(dataDir);
            case DATABASE -> new DatabaseSampleRepository(getOrInitConnection());
        };
    }

    public OrderRepository createOrderRepository() {
        return switch (type) {
            case MEMORY   -> new InMemoryOrderRepository();
            case FILE     -> new FileOrderRepository(dataDir);
            case JSON     -> new JsonOrderRepository(dataDir);
            case DATABASE -> new DatabaseOrderRepository(getOrInitConnection());
        };
    }

    private Connection getOrInitConnection() {
        if (connection == null) {
            try {
                connection = DatabaseConfig.openFile(dataDir);
                DatabaseConfig.initSchema(connection);
            } catch (SQLException e) {
                throw new RuntimeException("DB 연결 실패: " + dataDir, e);
            }
        }
        return connection;
    }
}
