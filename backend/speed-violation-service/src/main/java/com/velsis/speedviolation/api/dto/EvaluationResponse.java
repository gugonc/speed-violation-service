package com.velsis.speedviolation.api.dto;

import com.velsis.speedviolation.domain.model.EvaluationResult;
import com.velsis.speedviolation.domain.model.Severity;

import java.time.Instant;

public record EvaluationResponse(
        String licensePlate,
        String equipmentId,
        int measuredSpeed,
        int consideredSpeed,
        int speedLimit,
        double excessPercentage,
        boolean hasViolation,
        ViolationDetail violation,
        Instant processedAt
) {

    public record ViolationDetail(Severity severity, String ctbCode) {
    }

    public static EvaluationResponse from(EvaluationResult result) {
        ViolationDetail detail = result.hasViolation()
                ? new ViolationDetail(result.severity(), result.severity().ctbCode())
                : null;
        return new EvaluationResponse(
                result.licensePlate(),
                result.equipmentId(),
                result.measuredSpeed(),
                result.consideredSpeed(),
                result.speedLimit(),
                result.excessPercentage(),
                result.hasViolation(),
                detail,
                result.processedAt());
    }
}
