package org.example.repository.impl;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.persistence.DatabaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseOrderRepositoryTest {

    private Connection conn;
    private DatabaseOrderRepository repo;

    @BeforeEach
    void setUp() throws Exception {
        conn = DatabaseConfig.openMemory("order_test_" + System.nanoTime());
        DatabaseConfig.initSchema(conn);
        repo = new DatabaseOrderRepository(conn);
    }

    @Test
    @DisplayName("findByStatus 가 상태별로 주문을 올바르게 필터링한다")
    void databaseOrderRepository_findByStatus() {
        repo.save(new Order("ORD-001", "S-001", "고객A", 10, OrderStatus.RESERVED));
        repo.save(new Order("ORD-002", "S-001", "고객B", 20, OrderStatus.RESERVED));
        repo.save(new Order("ORD-003", "S-001", "고객C", 30, OrderStatus.CONFIRMED));

        List<Order> reserved = repo.findByStatus(OrderStatus.RESERVED);
        assertEquals(2, reserved.size());

        List<Order> confirmed = repo.findByStatus(OrderStatus.CONFIRMED);
        assertEquals(1, confirmed.size());
    }

    @Test
    @DisplayName("update 결과가 DB에 반영된다")
    void databaseOrderRepository_update() {
        Order order = new Order("ORD-001", "S-001", "고객A", 10, OrderStatus.RESERVED);
        repo.save(order);

        order.setStatus(OrderStatus.CONFIRMED);
        repo.update(order);

        assertEquals(OrderStatus.CONFIRMED, repo.findById("ORD-001").orElseThrow().getStatus());
        assertEquals(0, repo.findByStatus(OrderStatus.RESERVED).size());
    }
}
