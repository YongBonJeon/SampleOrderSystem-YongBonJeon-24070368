package org.example.generator;

import org.example.persistence.DatabaseConfig;

import java.nio.file.Path;
import java.sql.Connection;

public class DummyDataGenerator {

    public static void main(String[] args) throws Exception {
        String tables = "all";
        int count = 10;
        for (String arg : args) {
            if (arg.startsWith("--tables=")) tables = arg.substring(9);
            if (arg.startsWith("--count="))  count  = Integer.parseInt(arg.substring(8));
        }
        try (Connection conn = DatabaseConfig.openFile(Path.of("data"))) {
            DatabaseConfig.initSchema(conn);
            run(tables, count, conn);
        }
        System.out.printf("더미 데이터 삽입 완료 (tables=%s, count=%d)%n", tables, count);
    }

    public static void run(String tables, int count, Connection conn) {
        GeneratorRegistry registry = new GeneratorRegistry();
        registry.register(new SampleDataGenerator());
        registry.register(new OrderDataGenerator());
        registry.run(tables, conn, count);
    }
}
