package org.example.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseConfigTest {

    @Test
    @DisplayName("initSchema 실행 후 samples·orders 테이블이 존재한다")
    void databaseConfig_initSchema_createsBothTables() throws Exception {
        try (Connection conn = DatabaseConfig.openMemory("schema_test")) {
            DatabaseConfig.initSchema(conn);

            ResultSet rs = conn.getMetaData().getTables(null, null, null, new String[]{"TABLE"});
            boolean hasSamples = false;
            boolean hasOrders  = false;
            while (rs.next()) {
                String name = rs.getString("TABLE_NAME").toUpperCase();
                if (name.equals("SAMPLES")) hasSamples = true;
                if (name.equals("ORDERS"))  hasOrders  = true;
            }
            assertTrue(hasSamples, "samples 테이블이 존재해야 한다");
            assertTrue(hasOrders,  "orders 테이블이 존재해야 한다");
        }
    }
}
