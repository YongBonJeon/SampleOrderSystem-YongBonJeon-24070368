package org.example.generator;

import org.example.model.OrderStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class OrderDataGenerator implements DataGenerator {

    private static final String[] CUSTOMERS = {
        "삼성전자", "SK하이닉스", "DB하이텍", "한국반도체연구소", "서울대학교",
        "KAIST", "LG전자", "현대자동차", "포스코", "한화시스템"
    };

    @Override
    public String tableName() { return "orders"; }

    @Override
    public void generate(Connection conn, int count) {
        String sql = """
                INSERT INTO orders (order_id, sample_id, customer_name, quantity, status, ordered_at)
                VALUES (?,?,?,?,?,?)
                """;
        String date = LocalDate.now().toString().replace("-", "");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= count; i++) {
                ps.setString(1, String.format("ORD-%s-%04d", date, i));
                ps.setString(2, String.format("S-%04d", (i % 5) + 1));
                ps.setString(3, CUSTOMERS[(i - 1) % CUSTOMERS.length]);
                ps.setInt(4, (i % 10 + 1) * 10);
                ps.setString(5, OrderStatus.RESERVED.name());
                ps.setString(6, LocalDateTime.now().toString());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("주문 더미 데이터 삽입 실패", e);
        }
    }
}
