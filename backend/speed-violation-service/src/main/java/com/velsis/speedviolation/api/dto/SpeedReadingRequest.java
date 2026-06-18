package com.velsis.speedviolation.api.dto;

public record SpeedReadingRequest(
        String licensePlate,
        Integer measuredSpeed,
        Integer speedLimit,
        String equipmentId,
        String captureTimestamp
) {
}
