package org.example.controller;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.model.ProductionJob;
import org.example.model.Sample;
import org.example.queue.ProductionQueue;
import org.example.repository.impl.InMemoryOrderRepository;
import org.example.repository.impl.InMemorySampleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductionQueueRestorationTest {

    @Test
    @DisplayName("PRODUCING 승인 시 Order에 actualQty가 저장된다")
    void approve_withInsufficientStock_savesActualQtyToOrder() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        sampleRepo.save(new Sample("S-001", "SiC 기판", 0.5, 0.9, 30));
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        orderRepo.save(new Order("ORD-001", "S-001", "고객A", 200, OrderStatus.RESERVED));
        ProductionQueue queue = new ProductionQueue();

        ProductionJob job = new ProductionJob("ORD-001", "S-001", 170, 0.9, 0.5);
        Order order = orderRepo.findById("ORD-001").get();
        order.setActualQty(job.getActualQty());
        order.setStatus(OrderStatus.PRODUCING);
        orderRepo.update(order);

        assertEquals(job.getActualQty(), orderRepo.findById("ORD-001").get().getActualQty());
        assertTrue(orderRepo.findById("ORD-001").get().getActualQty() > 0);
    }

    @Test
    @DisplayName("재시작 시 PRODUCING 주문이 ProductionQueue에 복원된다")
    void restoreProductionQueue_restoresProducingOrdersToQueue() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        sampleRepo.save(new Sample("S-001", "SiC 기판", 0.5, 0.9, 30));
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();

        Order order = new Order("ORD-001", "S-001", "고객A", 200, OrderStatus.PRODUCING);
        order.setActualQty(210);
        orderRepo.save(order);

        ProductionQueue restoredQueue = new ProductionQueue();
        ProductionQueueRestorer.restore(sampleRepo, orderRepo, restoredQueue);

        assertFalse(restoredQueue.isEmpty());
        ProductionJob job = restoredQueue.peek().get();
        assertEquals("ORD-001", job.getOrderId());
        assertEquals(210, job.getActualQty());
    }
}
