package com.velsis.speedviolation.api;

import com.velsis.speedviolation.api.dto.SpeedReadingRequest;
import com.velsis.speedviolation.api.exception.ApiError;
import com.velsis.speedviolation.api.exception.ValidationException;
import com.velsis.speedviolation.domain.model.CaptureOrigin;
import com.velsis.speedviolation.domain.model.SpeedReadingCommand;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

@Component
public class SpeedReadingValidator {

    /** Formato antigo: 3 letras + 4 numeros (ex.: ABC1234). */
    private static final Pattern OLD_PLATE_PATTERN =
            Pattern.compile("^[A-Z]{3}[0-9]{4}$");

    /** Formato Mercosul: 3 letras + 1 numero + 1 letra + 2 numeros (ex.: ABC1D23). */
    private static final Pattern MERCOSUL_PLATE_PATTERN =
            Pattern.compile("^[A-Z]{3}[0-9][A-Z][0-9]{2}$");

    private final Clock clock;

    public SpeedReadingValidator(Clock clock) {
        this.clock = clock;
    }

    public SpeedReadingCommand validateAndBuild(SpeedReadingRequest request, String originHeader) {
        String plate = requireValidPlate(request.licensePlate());
        int measuredSpeed = requirePositive(request.measuredSpeed(),
                ApiError.INVALID_MEASURED_SPEED, "measuredSpeed");
        int speedLimit = requirePositive(request.speedLimit(),
                ApiError.INVALID_SPEED_LIMIT, "speedLimit");
        String equipmentId = requireNotBlank(request.equipmentId());
        Instant captureTimestamp = requireValidTimestamp(request.captureTimestamp());
        CaptureOrigin origin = requireValidOrigin(originHeader);

        return new SpeedReadingCommand(plate, measuredSpeed, speedLimit,
                equipmentId, captureTimestamp, origin);
    }

    static boolean isValidPlate(String plate) {
        return plate != null
                && (OLD_PLATE_PATTERN.matcher(plate).matches()
                || MERCOSUL_PLATE_PATTERN.matcher(plate).matches());
    }

    private String requireValidPlate(String plate) {
        if (!isValidPlate(plate)) {
            throw new ValidationException(ApiError.INVALID_LICENSE_PLATE,
                    "Invalid license plate format");
        }
        return plate;
    }

    private int requirePositive(Integer value, ApiError error, String field) {
        if (value == null || value <= 0) {
            throw new ValidationException(error, field + " must be a positive integer");
        }
        return value;
    }

    private String requireNotBlank(String equipmentId) {
        if (equipmentId == null || equipmentId.isBlank()) {
            throw new ValidationException(ApiError.INVALID_EQUIPMENT_ID,
                    "equipmentId must not be blank");
        }
        return equipmentId;
    }

    private Instant requireValidTimestamp(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException(ApiError.INVALID_CAPTURE_TIMESTAMP,
                    "captureTimestamp is required");
        }
        Instant timestamp;
        try {
            timestamp = OffsetDateTime.parse(raw).toInstant();
        } catch (DateTimeParseException ex) {
            throw new ValidationException(ApiError.INVALID_CAPTURE_TIMESTAMP,
                    "captureTimestamp must be a valid ISO-8601 date-time with offset");
        }
        if (timestamp.isAfter(clock.instant())) {
            throw new ValidationException(ApiError.INVALID_CAPTURE_TIMESTAMP,
                    "captureTimestamp must not be in the future");
        }
        return timestamp;
    }

    private CaptureOrigin requireValidOrigin(String originHeader) {
        if (originHeader == null || originHeader.isBlank()) {
            throw new ValidationException(ApiError.INVALID_ORIGIN,
                    "x-origin header is required");
        }
        try {
            return CaptureOrigin.valueOf(originHeader);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException(ApiError.INVALID_ORIGIN,
                    "x-origin must be one of FIXED, MOBILE, HANDHELD");
        }
    }
}
