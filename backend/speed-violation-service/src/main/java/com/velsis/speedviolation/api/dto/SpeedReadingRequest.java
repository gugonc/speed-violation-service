package com.velsis.speedviolation.api.dto;

public record SpeedReadingRequest(
        String licensePlate,
        Double measuredSpeed,
        Double speedLimit,
        String equipmentId,
        String captureTimestamp
) {
}
