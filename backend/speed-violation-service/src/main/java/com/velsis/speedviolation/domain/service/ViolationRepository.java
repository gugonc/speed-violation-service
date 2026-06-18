package com.velsis.speedviolation.domain.service;

import com.velsis.speedviolation.domain.model.Violation;

import java.util.List;

public interface ViolationRepository {

    void save(Violation violation);

    List<Violation> findByLicensePlate(String licensePlate);
}
