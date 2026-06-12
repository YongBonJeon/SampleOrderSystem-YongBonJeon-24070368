package org.example.repository;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.repository.impl.InMemoryOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryOrderRepositoryTest {

    private OrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryOrderRepository();
    }

    @Test
    void save_thenFindById_returnsStoredOrder() {
        Order order = new Order("ORD-20260612-0001", "S-001", "삼성전자", 100, OrderStatus.RESERVED);

        repository.save(order);
        Optional<Order> result = repository.findById("ORD-20260612-0001");

        assertTrue(result.isPresent());
        assertEquals("ORD-20260612-0001", result.get().getOrderId());
        assertEquals(OrderStatus.RESERVED, result.get().getStatus());
    }

    @Test
    void findByStatus_returnsOnlyMatchingOrders() {
        repository.save(new Order("ORD-20260612-0001", "S-001", "삼성전자", 100, OrderStatus.RESERVED));
        repository.save(new Order("ORD-20260612-0002", "S-002", "SK하이닉스", 50, OrderStatus.CONFIRMED));
        repository.save(new Order("ORD-20260612-0003", "S-001", "LG이노텍", 200, OrderStatus.RESERVED));

        List<Order> reserved = repository.findByStatus(OrderStatus.RESERVED);

        assertEquals(2, reserved.size());
        assertTrue(reserved.stream().allMatch(o -> o.getStatus() == OrderStatus.RESERVED));
    }

    @Test
    void update_changesOrderStatus() {
        repository.save(new Order("ORD-20260612-0001", "S-001", "삼성전자", 100, OrderStatus.RESERVED));

        Order updated = new Order("ORD-20260612-0001", "S-001", "삼성전자", 100, OrderStatus.CONFIRMED);
        repository.update(updated);

        Optional<Order> result = repository.findById("ORD-20260612-0001");
        assertTrue(result.isPresent());
        assertEquals(OrderStatus.CONFIRMED, result.get().getStatus());
    }

    @Test
    void findAll_returnsAllSavedOrders() {
        repository.save(new Order("ORD-20260612-0001", "S-001", "삼성전자", 100, OrderStatus.RESERVED));
        repository.save(new Order("ORD-20260612-0002", "S-002", "SK하이닉스", 50, OrderStatus.CONFIRMED));

        List<Order> result = repository.findAll();

        assertEquals(2, result.size());
    }
}
