package org.example.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderIdGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final LocalDate date;
    private final Map<String, Integer> sequences = new HashMap<>();

    public OrderIdGenerator(LocalDate date, List<String> existingOrderIds) {
        this.date = date;
        String dateKey = date.format(DATE_FORMAT);
        String prefix = "ORD-" + dateKey + "-";
        existingOrderIds.stream()
                .filter(id -> id.startsWith(prefix))
                .mapToInt(id -> {
                    try { return Integer.parseInt(id.substring(prefix.length())); }
                    catch (NumberFormatException e) { return 0; }
                })
                .max()
                .ifPresent(max -> sequences.put(dateKey, max));
    }

    public OrderIdGenerator(LocalDate date) {
        this(date, List.of());
    }

    public OrderIdGenerator() {
        this(LocalDate.now(), List.of());
    }

    public String generate() {
        String dateKey = date.format(DATE_FORMAT);
        int seq = sequences.merge(dateKey, 1, Integer::sum);
        return String.format("ORD-%s-%04d", dateKey, seq);
    }
}
