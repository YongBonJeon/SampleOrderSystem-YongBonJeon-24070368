package org.example.repository.impl;

import org.example.model.Sample;
import org.example.repository.SampleRepository;

import java.sql.*;
import java.util.*;

public class DatabaseSampleRepository implements SampleRepository {

    private final Connection conn;

    public DatabaseSampleRepository(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Sample save(Sample sample) {
        String sql = "MERGE INTO samples (id, name, avg_production_time, yield_rate, stock) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sample.getId());
            ps.setString(2, sample.getName());
            ps.setDouble(3, sample.getAvgProductionTime());
            ps.setDouble(4, sample.getYieldRate());
            ps.setInt(5, sample.getStock());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("시료 저장 실패", e);
        }
        return sample;
    }

    @Override
    public Optional<Sample> findById(String id) {
        String sql = "SELECT * FROM samples WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("시료 조회 실패", e);
        }
    }

    @Override
    public List<Sample> findAll() {
        String sql = "SELECT * FROM samples";
        List<Sample> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("시료 전체 조회 실패", e);
        }
        return list;
    }

    @Override
    public Sample update(Sample sample) {
        return save(sample);
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM samples WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("시료 삭제 실패", e);
        }
    }

    private Sample map(ResultSet rs) throws SQLException {
        return new Sample(
                rs.getString("id"),
                rs.getString("name"),
                rs.getDouble("avg_production_time"),
                rs.getDouble("yield_rate"),
                rs.getInt("stock")
        );
    }
}
