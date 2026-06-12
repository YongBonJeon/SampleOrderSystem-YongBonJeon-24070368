package org.example.repository.impl;

import org.example.model.Sample;
import org.example.repository.SampleRepository;

import java.util.*;

public class InMemorySampleRepository implements SampleRepository {

    private final Map<String, Sample> store = new LinkedHashMap<>();

    @Override
    public Sample save(Sample sample) {
        store.put(sample.getId(), sample);
        return sample;
    }

    @Override
    public Optional<Sample> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Sample> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Sample update(Sample sample) {
        store.put(sample.getId(), sample);
        return sample;
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
