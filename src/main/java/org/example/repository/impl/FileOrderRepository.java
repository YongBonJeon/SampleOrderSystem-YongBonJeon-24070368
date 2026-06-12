package org.example.repository.impl;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.repository.OrderRepository;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileOrderRepository implements OrderRepository {

    private final Path filePath;

    public FileOrderRepository(Path dataDir) {
        this.filePath = dataDir.resolve("orders.dat");
    }

    @Override
    public Order save(Order order) {
        Map<String, Order> store = load();
        store.put(order.getOrderId(), order);
        persist(store);
        return order;
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(load().get(orderId));
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(load().values());
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return load().values().stream()
                .filter(o -> o.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public Order update(Order order) {
        return save(order);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Order> load() {
        if (!Files.exists(filePath)) return new LinkedHashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toFile()))) {
            return (Map<String, Order>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("주문 파일 읽기 실패: " + filePath, e);
        }
    }

    private void persist(Map<String, Order> store) {
        try {
            Files.createDirectories(filePath.getParent());
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
                oos.writeObject(store);
            }
        } catch (IOException e) {
            throw new RuntimeException("주문 파일 쓰기 실패: " + filePath, e);
        }
    }
}
