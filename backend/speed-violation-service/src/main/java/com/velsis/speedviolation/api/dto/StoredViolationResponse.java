package com.velsis.speedviolation.api.dto;

import com.velsis.speedviolation.domain.model.Severity;
import com.velsis.speedviolation.domain.model.Violation;

import java.time.Instant;

public record StoredViolationResponse(
        String licensePlate,
        String equipmentId,
        int measuredSpeed,
        int consideredSpeed,
        int speedLimit,
        double excessPercentage,
        Severity severity,
        String ctbCode,
        Instant captureTimestamp,
        Instant processedAt
) {

    public static StoredViolationResponse from(Violation violation) {
        return new StoredViolationResponse(
                violation.licensePlate(),
                violation.equipmentId(),
                violation.measuredSpeed(),
                violation.consideredSpeed(),
                violation.speedLimit(),
                violation.excessPercentage(),
                violation.severity(),
                violation.ctbCode(),
                violation.captureTimestamp(),
                violation.processedAt());
    }
}
