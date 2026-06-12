package org.example.generator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SampleDataGenerator implements DataGenerator {

    private static final String[] NAMES = {
        "SiC 파워기판-6인치", "산화막 웨이퍼-SiO2", "GaN 에피택셜-4인치",
        "실리콘 웨이퍼-8인치", "SiC 쇼트키 다이오드", "GaAs 기판-2인치",
        "InP 웨이퍼-3인치", "포토레지스트-PR7", "질화막 웨이퍼-Si3N4", "사파이어 기판-2인치"
    };

    @Override
    public String tableName() { return "samples"; }

    @Override
    public void generate(Connection conn, int count) {
        String sql = "INSERT INTO samples (id, name, avg_production_time, yield_rate, stock) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= count; i++) {
                ps.setString(1, String.format("S-%04d", i));
                ps.setString(2, NAMES[(i - 1) % NAMES.length]);
                ps.setDouble(3, 0.3 + (i % 5) * 0.2);
                ps.setDouble(4, 0.80 + (i % 3) * 0.05);
                ps.setInt(5, (i % 10) * 20);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("시료 더미 데이터 삽입 실패", e);
        }
    }
}
