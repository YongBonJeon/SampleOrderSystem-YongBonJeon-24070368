package org.example.queue;

import org.example.model.ProductionJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductionQueueTest {

    private ProductionJob sampleJob(String orderId) {
        return new ProductionJob(orderId, "S-001", 170, 0.9, 0.5);
    }

    @Test
    @DisplayName("두 번째 등록 작업은 getWaiting()에 포함된다")
    void enqueue_secondJob_isInWaitingList() {
        ProductionQueue queue = new ProductionQueue();
        queue.enqueue(sampleJob("ORD-20260612-0001"));
        queue.enqueue(sampleJob("ORD-20260612-0002"));

        List<ProductionJob> waiting = queue.getWaiting();

        assertEquals(1, waiting.size());
        assertEquals("ORD-20260612-0002", waiting.get(0).getOrderId());
    }

    @Test
    @DisplayName("첫 번째 등록 작업이 peek()으로 조회된다")
    void enqueue_firstJob_isCurrentJob() {
        ProductionQueue queue = new ProductionQueue();
        ProductionJob job = sampleJob("ORD-20260612-0001");

        queue.enqueue(job);

        assertTrue(queue.peek().isPresent());
        assertEquals("ORD-20260612-0001", queue.peek().get().getOrderId());
    }
}
