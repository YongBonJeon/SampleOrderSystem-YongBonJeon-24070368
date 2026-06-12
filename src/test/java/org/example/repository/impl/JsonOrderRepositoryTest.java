package org.example.repository.impl;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonOrderRepositoryTest {

    @Test
    @DisplayName("orderedAt 포함 주문의 findByStatus와 update가 재시작 후에도 유지된다")
    void jsonOrderRepository_statusAndUpdatePersist(@TempDir Path tempDir) {
        JsonOrderRepository repo = new JsonOrderRepository(tempDir);
        Order order = new Order("ORD-001", "S-001", "고객A", 10, OrderStatus.RESERVED);
        repo.save(order);

        order.setStatus(OrderStatus.CONFIRMED);
        repo.update(order);

        List<Order> confirmed = new JsonOrderRepository(tempDir).findByStatus(OrderStatus.CONFIRMED);
        assertEquals(1, confirmed.size());
        assertEquals("ORD-001", confirmed.get(0).getOrderId());
        assertNotNull(confirmed.get(0).getOrderedAt());
    }

    @Test
    @DisplayName("여러 상태 주문 저장 후 findByStatus 가 올바르게 필터링된다")
    void jsonOrderRepository_findByStatus_filtersCorrectly(@TempDir Path tempDir) {
        JsonOrderRepository repo = new JsonOrderRepository(tempDir);
        repo.save(new Order("ORD-001", "S-001", "고객A", 10, OrderStatus.RESERVED));
        repo.save(new Order("ORD-002", "S-001", "고객B", 20, OrderStatus.RESERVED));
        repo.save(new Order("ORD-003", "S-001", "고객C", 30, OrderStatus.CONFIRMED));

        List<Order> reserved = new JsonOrderRepository(tempDir).findByStatus(OrderStatus.RESERVED);

        assertEquals(2, reserved.size());
    }
}
