package org.example.repository.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.example.model.Sample;
import org.example.repository.SampleRepository;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class JsonSampleRepository implements SampleRepository {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<LinkedHashMap<String, Sample>>() {}.getType();

    private final Path filePath;

    public JsonSampleRepository(Path dataDir) {
        this.filePath = dataDir.resolve("samples.json");
    }

    @Override
    public Sample save(Sample sample) {
        Map<String, Sample> store = load();
        store.put(sample.getId(), sample);
        persist(store);
        return sample;
    }

    @Override
    public Optional<Sample> findById(String id) {
        return Optional.ofNullable(load().get(id));
    }

    @Override
    public List<Sample> findAll() {
        return new ArrayList<>(load().values());
    }

    @Override
    public Sample update(Sample sample) {
        return save(sample);
    }

    @Override
    public void deleteById(String id) {
        Map<String, Sample> store = load();
        store.remove(id);
        persist(store);
    }

    private Map<String, Sample> load() {
        if (!Files.exists(filePath)) return new LinkedHashMap<>();
        try {
            String json = Files.readString(filePath, StandardCharsets.UTF_8);
            Map<String, Sample> result = GSON.fromJson(json, MAP_TYPE);
            return result != null ? result : new LinkedHashMap<>();
        } catch (IOException e) {
            throw new RuntimeException("시료 JSON 읽기 실패: " + filePath, e);
        }
    }

    private void persist(Map<String, Sample> store) {
        try {
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, GSON.toJson(store), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("시료 JSON 쓰기 실패: " + filePath, e);
        }
    }
}
