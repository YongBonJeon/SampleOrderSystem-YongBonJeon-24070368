package org.example.repository.impl;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.repository.OrderRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseOrderRepository implements OrderRepository {

    private final Connection conn;

    public DatabaseOrderRepository(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Order save(Order order) {
        String sql = """
                MERGE INTO orders (order_id, sample_id, customer_name, quantity, status, ordered_at, actual_qty)
                VALUES (?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, order.getOrderId());
            ps.setString(2, order.getSampleId());
            ps.setString(3, order.getCustomerName());
            ps.setInt(4, order.getQuantity());
            ps.setString(5, order.getStatus().name());
            ps.setString(6, order.getOrderedAt().toString());
            ps.setInt(7, order.getActualQty());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("주문 저장 실패", e);
        }
        return order;
    }

    @Override
    public Optional<Order> findById(String orderId) {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("주문 조회 실패", e);
        }
    }

    @Override
    public List<Order> findAll() {
        String sql = "SELECT * FROM orders";
        List<Order> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("주문 전체 조회 실패", e);
        }
        return list;
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return findAll().stream()
                .filter(o -> o.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public Order update(Order order) {
        return save(order);
    }

    private Order map(ResultSet rs) throws SQLException {
        Order order = new Order(
                rs.getString("order_id"),
                rs.getString("sample_id"),
                rs.getString("customer_name"),
                rs.getInt("quantity"),
                OrderStatus.valueOf(rs.getString("status"))
        );
        order.setActualQty(rs.getInt("actual_qty"));
        return order;
    }
}
