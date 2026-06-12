package org.example.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class OrderIdGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final LocalDate date;
    private final Map<String, Integer> sequences = new HashMap<>();

    public OrderIdGenerator(LocalDate date) {
        this.date = date;
    }

    public OrderIdGenerator() {
        this(LocalDate.now());
    }

    public String generate() {
        String dateKey = date.format(DATE_FORMAT);
        int seq = sequences.merge(dateKey, 1, Integer::sum);
        return String.format("ORD-%s-%04d", dateKey, seq);
    }
}
