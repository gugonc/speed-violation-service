package com.velsis.speedviolation.domain.model;

import java.time.Instant;

public record Violation(
        String licensePlate,
        String equipmentId,
        int measuredSpeed,
        int consideredSpeed,
        int speedLimit,
        double excessPercentage,
        Severity severity,
        Instant captureTimestamp,
        Instant processedAt
) {

    public String ctbCode() {
        return severity.ctbCode();
    }
}
