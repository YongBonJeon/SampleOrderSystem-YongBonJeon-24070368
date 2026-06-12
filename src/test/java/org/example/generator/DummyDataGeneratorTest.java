package org.example.generator;

import org.example.persistence.DatabaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DummyDataGeneratorTest {

    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        conn = DatabaseConfig.openMemory("gen_test_" + System.nanoTime());
        DatabaseConfig.initSchema(conn);
    }

    @Test
    @DisplayName("--tables=all --count=5 실행 후 samples·orders 각 5건이 삽입된다")
    void dummyDataGenerator_insertsExpectedCountPerTable() throws Exception {
        DummyDataGenerator.run("all", 5, conn);

        ResultSet rs1 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM samples");
        rs1.next();
        assertEquals(5, rs1.getInt(1));

        ResultSet rs2 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM orders");
        rs2.next();
        assertEquals(5, rs2.getInt(1));
    }
}
