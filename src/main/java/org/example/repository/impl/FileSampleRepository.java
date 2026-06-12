package org.example.repository.impl;

import org.example.model.Sample;
import org.example.repository.SampleRepository;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileSampleRepository implements SampleRepository {

    private final Path filePath;

    public FileSampleRepository(Path dataDir) {
        this.filePath = dataDir.resolve("samples.dat");
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

    @SuppressWarnings("unchecked")
    private Map<String, Sample> load() {
        if (!Files.exists(filePath)) return new LinkedHashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toFile()))) {
            return (Map<String, Sample>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("시료 파일 읽기 실패: " + filePath, e);
        }
    }

    private void persist(Map<String, Sample> store) {
        try {
            Files.createDirectories(filePath.getParent());
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
                oos.writeObject(store);
            }
        } catch (IOException e) {
            throw new RuntimeException("시료 파일 쓰기 실패: " + filePath, e);
        }
    }
}
