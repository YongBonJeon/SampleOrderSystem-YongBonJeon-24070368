package org.example.generator;

import java.sql.Connection;

public interface DataGenerator {
    String tableName();
    void generate(Connection conn, int count);
}
