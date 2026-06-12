package org.example.controller;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.model.Sample;
import org.example.queue.ProductionQueue;
import org.example.repository.impl.InMemoryOrderRepository;
import org.example.repository.impl.InMemorySampleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProductionQueueRestorerTest {

    @Test
    @DisplayName("재시작 후 복원 시 startedAt이 있는 주문이 큐 head가 된다")
    void restore_activeJobComesFirst_regardlessOfOrderedAt() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        sampleRepo.save(new Sample("S-001", "웨이퍼A", 0.5, 0.9, 0));

        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();

        // B: 더 이른 orderedAt, 나중에 승인 → startedAt=null (대기 중)
        Order orderB = new Order("ORD-001", "S-001", "고객B", 100, OrderStatus.PRODUCING);
        orderB.setActualQty(112);
        // orderedAt 기본값(now)이 더 이름 — setOrderedAt으로 과거로 설정
        orderB.setOrderedAt(LocalDateTime.of(2026, 6, 12, 9, 0, 0));
        orderB.setStartedAt(null);
        orderRepo.save(orderB);

        // A: 늦은 orderedAt, 먼저 승인 → startedAt 있음 (생산 중)
        Order orderA = new Order("ORD-002", "S-001", "고객A", 100, OrderStatus.PRODUCING);
        orderA.setActualQty(112);
        orderA.setOrderedAt(LocalDateTime.of(2026, 6, 12, 10, 0, 0));
        orderA.setStartedAt(LocalDateTime.of(2026, 6, 12, 11, 0, 0));
        orderRepo.save(orderA);

        ProductionQueue queue = new ProductionQueue();
        ProductionQueueRestorer.restore(sampleRepo, orderRepo, queue);

        // A(startedAt 있음)가 head여야 한다
        assertEquals("ORD-002", queue.peek().get().getOrderId(), "startedAt이 있는 주문이 큐 head여야 한다");
        assertNotNull(queue.peek().get().getStartedAt(), "head의 startedAt이 복원되어야 한다");
    }
}
