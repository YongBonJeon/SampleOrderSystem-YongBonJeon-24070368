package org.example.queue;

import org.example.model.ProductionJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductionQueueTest {

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 6, 12, 9, 0);

    private ProductionJob sampleJob(String orderId) {
        return new ProductionJob(orderId, "S-001", 170, 0.9, 0.5);
    }

    @Test
    @DisplayName("dequeue() 호출 시 첫 번째 작업을 반환하고 큐에서 제거한다")
    void dequeue_returnsAndRemovesFirst() {
        ProductionQueue queue = new ProductionQueue();
        queue.enqueue(sampleJob("ORD-20260612-0001"));
        queue.enqueue(sampleJob("ORD-20260612-0002"));

        ProductionJob dequeued = queue.dequeue();

        assertEquals("ORD-20260612-0001", dequeued.getOrderId());
        assertEquals("ORD-20260612-0002", queue.peek().get().getOrderId());
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

    @Test
    @DisplayName("첫 번째 enqueue 시 startedAt이 clock 시각으로 설정된다")
    void enqueue_firstJob_setsStartedAt() {
        ProductionQueue queue = new ProductionQueue(() -> FIXED_TIME);
        ProductionJob job = sampleJob("ORD-20260612-0001");

        queue.enqueue(job);

        assertEquals(FIXED_TIME, job.getStartedAt());
    }

    @Test
    @DisplayName("두 번째 enqueue 시 두 번째 작업의 startedAt은 null이다")
    void enqueue_secondJob_doesNotSetStartedAt() {
        ProductionQueue queue = new ProductionQueue(() -> FIXED_TIME);
        queue.enqueue(sampleJob("ORD-20260612-0001"));
        ProductionJob second = sampleJob("ORD-20260612-0002");

        queue.enqueue(second);

        assertNull(second.getStartedAt());
    }

    @Test
    @DisplayName("dequeue() 후 다음 작업에 startedAt이 설정된다")
    void dequeue_setsStartedAtOnNextJob() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 12, 9, 0);
        LocalDateTime nextStart = LocalDateTime.of(2026, 6, 12, 11, 0);
        int[] callCount = {0};
        ProductionQueue queue = new ProductionQueue(() -> callCount[0]++ == 0 ? start : nextStart);

        ProductionJob first = sampleJob("ORD-20260612-0001");
        ProductionJob second = sampleJob("ORD-20260612-0002");
        queue.enqueue(first);  // callCount[0] = 1 → first.startedAt = start
        queue.enqueue(second);

        queue.dequeue();       // callCount[0] = 2 → second.startedAt = nextStart

        assertEquals(nextStart, second.getStartedAt());
    }
}
