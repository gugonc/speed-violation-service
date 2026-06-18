package com.velsis.speedviolation.domain.model;

import java.time.Instant;

public record SpeedReadingCommand(
        String licensePlate,
        int measuredSpeed,
        int speedLimit,
        String equipmentId,
        Instant captureTimestamp,
        CaptureOrigin origin
) {
}
