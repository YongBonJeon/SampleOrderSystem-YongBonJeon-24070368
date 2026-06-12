package org.example.generator;

import java.sql.Connection;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class GeneratorRegistry {

    private final Map<String, DataGenerator> generators = new LinkedHashMap<>();

    public void register(DataGenerator gen) {
        generators.put(gen.tableName(), gen);
    }

    public void run(String tables, Connection conn, int count) {
        if ("all".equalsIgnoreCase(tables)) {
            generators.values().forEach(g -> g.generate(conn, count));
        } else {
            Arrays.stream(tables.split(","))
                    .map(String::trim)
                    .filter(generators::containsKey)
                    .forEach(t -> generators.get(t).generate(conn, count));
        }
    }
}
