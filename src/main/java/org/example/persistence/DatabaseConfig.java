package org.example.persistence;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {

    public static Connection openFile(Path dataDir) throws SQLException {
        String url = "jdbc:h2:" + dataDir.toAbsolutePath() + "/ssemi";
        return DriverManager.getConnection(url, "sa", "");
    }

    public static Connection openMemory(String dbName) throws SQLException {
        String url = "jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1";
        return DriverManager.getConnection(url, "sa", "");
    }

    public static void initSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS samples (
                    id                  VARCHAR(50)  PRIMARY KEY,
                    name                VARCHAR(200) NOT NULL,
                    avg_production_time DOUBLE       NOT NULL,
                    yield_rate          DOUBLE       NOT NULL,
                    stock               INT          NOT NULL
                )
                """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    order_id      VARCHAR(50)  PRIMARY KEY,
                    sample_id     VARCHAR(50)  NOT NULL,
                    customer_name VARCHAR(200) NOT NULL,
                    quantity      INT          NOT NULL,
                    status        VARCHAR(20)  NOT NULL,
                    ordered_at    VARCHAR(30)  NOT NULL,
                    actual_qty    INT          NOT NULL DEFAULT 0
                )
                """);
        }
    }
}
