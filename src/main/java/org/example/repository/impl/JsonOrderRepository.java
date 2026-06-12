package org.example.repository.impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.repository.OrderRepository;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class JsonOrderRepository implements OrderRepository {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, t, ctx) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, t, ctx) -> LocalDateTime.parse(json.getAsString()))
            .create();
    private static final Type MAP_TYPE = new TypeToken<LinkedHashMap<String, Order>>() {}.getType();

    private final Path filePath;

    public JsonOrderRepository(Path dataDir) {
        this.filePath = dataDir.resolve("orders.json");
    }

    @Override
    public Order save(Order order) {
        Map<String, Order> store = load();
        store.put(order.getOrderId(), order);
        persist(store);
        return order;
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(load().get(orderId));
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(load().values());
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return load().values().stream()
                .filter(o -> o.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public Order update(Order order) {
        return save(order);
    }

    private Map<String, Order> load() {
        if (!Files.exists(filePath)) return new LinkedHashMap<>();
        try {
            String json = Files.readString(filePath, StandardCharsets.UTF_8);
            Map<String, Order> result = GSON.fromJson(json, MAP_TYPE);
            return result != null ? result : new LinkedHashMap<>();
        } catch (IOException e) {
            throw new RuntimeException("주문 JSON 읽기 실패: " + filePath, e);
        }
    }

    private void persist(Map<String, Order> store) {
        try {
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, GSON.toJson(store), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("주문 JSON 쓰기 실패: " + filePath, e);
        }
    }
}
