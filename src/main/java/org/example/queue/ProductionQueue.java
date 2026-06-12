package org.example.queue;

import org.example.model.ProductionJob;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ProductionQueue {

    private final LinkedList<ProductionJob> queue = new LinkedList<>();

    public void enqueue(ProductionJob job) {
        queue.addLast(job);
    }

    public Optional<ProductionJob> peek() {
        return Optional.ofNullable(queue.peekFirst());
    }

    public List<ProductionJob> getWaiting() {
        if (queue.size() <= 1) return List.of();
        return List.copyOf(queue.subList(1, queue.size()));
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
