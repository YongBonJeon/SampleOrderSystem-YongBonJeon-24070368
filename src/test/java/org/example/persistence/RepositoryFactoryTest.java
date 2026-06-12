package org.example.persistence;

import org.example.repository.OrderRepository;
import org.example.repository.SampleRepository;
import org.example.repository.impl.InMemoryOrderRepository;
import org.example.repository.impl.InMemorySampleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RepositoryFactoryTest {

    @Test
    @DisplayName("MEMORY 타입으로 생성한 SampleRepository는 InMemory 구현체다")
    void createSampleRepository_withMemoryType_returnsInMemoryInstance() {
        RepositoryFactory factory = new RepositoryFactory(PersistenceType.MEMORY);

        SampleRepository repo = factory.createSampleRepository();

        assertInstanceOf(InMemorySampleRepository.class, repo);
    }

    @Test
    @DisplayName("MEMORY 타입으로 생성한 OrderRepository는 InMemory 구현체다")
    void createOrderRepository_withMemoryType_returnsInMemoryInstance() {
        RepositoryFactory factory = new RepositoryFactory(PersistenceType.MEMORY);

        OrderRepository repo = factory.createOrderRepository();

        assertInstanceOf(InMemoryOrderRepository.class, repo);
    }
}
