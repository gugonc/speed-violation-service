package com.velsis.speedviolation.infrastructure;

import com.velsis.speedviolation.domain.model.Violation;
import com.velsis.speedviolation.domain.service.ViolationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class InMemoryViolationRepository implements ViolationRepository {

    private final ConcurrentMap<String, List<Violation>> store = new ConcurrentHashMap<>();

    @Override
    public void save(Violation violation) {
        store.computeIfAbsent(violation.licensePlate(), key -> new CopyOnWriteArrayList<>())
                .add(violation);
    }

    @Override
    public List<Violation> findByLicensePlate(String licensePlate) {
        return List.copyOf(store.getOrDefault(licensePlate, List.of()));
    }
}
