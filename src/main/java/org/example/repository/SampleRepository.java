package org.example.repository;

import org.example.model.Sample;

import java.util.List;
import java.util.Optional;

public interface SampleRepository {
    Sample save(Sample sample);
    Optional<Sample> findById(String id);
    List<Sample> findAll();
    Sample update(Sample sample);
    void deleteById(String id);
}
