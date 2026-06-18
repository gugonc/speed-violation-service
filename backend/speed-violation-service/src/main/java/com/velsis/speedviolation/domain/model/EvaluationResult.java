package com.velsis.speedviolation.domain.model;

import java.time.Instant;

public record EvaluationResult(
        String licensePlate,
        String equipmentId,
        int measuredSpeed,
        int consideredSpeed,
        int speedLimit,
        double excessPercentage,
        boolean hasViolation,
        Severity severity,
        Instant captureTimestamp,
        Instant processedAt
) {

    public static EvaluationResult noViolation(SpeedReadingCommand command,
                                               int consideredSpeed,
                                               Instant processedAt) {
        return new EvaluationResult(
                command.licensePlate(),
                command.equipmentId(),
                command.measuredSpeed(),
                consideredSpeed,
                command.speedLimit(),
                0.0,
                false,
                null,
                command.captureTimestamp(),
                processedAt);
    }

    public static EvaluationResult withViolation(SpeedReadingCommand command,
                                                 int consideredSpeed,
                                                 double excessPercentage,
                                                 Severity severity,
                                                 Instant processedAt) {
        return new EvaluationResult(
                command.licensePlate(),
                command.equipmentId(),
                command.measuredSpeed(),
                consideredSpeed,
                command.speedLimit(),
                excessPercentage,
                true,
                severity,
                command.captureTimestamp(),
                processedAt);
    }

    public Violation toViolation() {
        return new Violation(
                licensePlate,
                equipmentId,
                measuredSpeed,
                consideredSpeed,
                speedLimit,
                excessPercentage,
                severity,
                captureTimestamp,
                processedAt);
    }
}
