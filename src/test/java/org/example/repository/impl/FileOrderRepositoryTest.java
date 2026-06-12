package org.example.repository.impl;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileOrderRepositoryTest {

    @Test
    @DisplayName("save 후 새 인스턴스로 재조회 시 데이터 유지 및 findByStatus 동작")
    void fileOrderRepository_survivesRestartAndFiltersByStatus(@TempDir Path tempDir) {
        FileOrderRepository repo = new FileOrderRepository(tempDir);
        repo.save(new Order("ORD-001", "S-001", "고객A", 10, OrderStatus.RESERVED));
        repo.save(new Order("ORD-002", "S-001", "고객B", 20, OrderStatus.RESERVED));
        repo.save(new Order("ORD-003", "S-001", "고객C", 30, OrderStatus.CONFIRMED));

        List<Order> reserved = new FileOrderRepository(tempDir).findByStatus(OrderStatus.RESERVED);

        assertEquals(2, reserved.size());
        assertTrue(reserved.stream().noneMatch(o -> o.getStatus() == OrderStatus.CONFIRMED));
    }

    @Test
    @DisplayName("status 변경 후 새 인스턴스로 재조회하면 변경된 상태가 유지된다")
    void fileOrderRepository_updatePersistsAcrossRestart(@TempDir Path tempDir) {
        FileOrderRepository repo = new FileOrderRepository(tempDir);
        Order order = new Order("ORD-001", "S-001", "고객A", 10, OrderStatus.RESERVED);
        repo.save(order);

        order.setStatus(OrderStatus.CONFIRMED);
        repo.update(order);

        Order reloaded = new FileOrderRepository(tempDir).findById("ORD-001").orElseThrow();
        assertEquals(OrderStatus.CONFIRMED, reloaded.getStatus());
    }
}
