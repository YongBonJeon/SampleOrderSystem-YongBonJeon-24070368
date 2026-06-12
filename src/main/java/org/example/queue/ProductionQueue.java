package org.example.queue;

import org.example.model.ProductionJob;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class ProductionQueue {

    private final LinkedList<ProductionJob> queue = new LinkedList<>();
    private final Supplier<LocalDateTime> clock;

    public ProductionQueue() {
        this(LocalDateTime::now);
    }

    public ProductionQueue(Supplier<LocalDateTime> clock) {
        this.clock = clock;
    }

    public void enqueue(ProductionJob job) {
        boolean wasEmpty = queue.isEmpty();
        queue.addLast(job);
        if (wasEmpty) {
            job.setStartedAt(clock.get());
        }
    }

    public Optional<ProductionJob> peek() {
        return Optional.ofNullable(queue.peekFirst());
    }

    public List<ProductionJob> getWaiting() {
        if (queue.size() <= 1) return List.of();
        return List.copyOf(queue.subList(1, queue.size()));
    }

    public ProductionJob dequeue() {
        if (queue.isEmpty()) throw new IllegalStateException("생산 큐가 비어있습니다.");
        ProductionJob removed = queue.removeFirst();
        if (!queue.isEmpty()) {
            queue.peekFirst().setStartedAt(clock.get());
        }
        return removed;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
