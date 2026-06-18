package com.velsis.speedviolation.api;

import com.velsis.speedviolation.api.dto.SpeedReadingRequest;
import com.velsis.speedviolation.api.exception.ApiError;
import com.velsis.speedviolation.api.exception.ValidationException;
import com.velsis.speedviolation.domain.model.CaptureOrigin;
import com.velsis.speedviolation.domain.model.SpeedReadingCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpeedReadingValidatorTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-06-10T12:00:00Z");

    private final Clock clock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
    private final SpeedReadingValidator validator = new SpeedReadingValidator(clock);

    private SpeedReadingRequest request(String plate, Double measured, Double limit,
                                        String equipmentId, String timestamp) {
        return new SpeedReadingRequest(plate, measured, limit, equipmentId, timestamp);
    }

    private SpeedReadingRequest validRequest() {
        return request("ABC1D23", 92.0, 60.0, "RAD-CWB-001", "2026-06-08T14:30:00Z");
    }

    private void assertError(SpeedReadingRequest req, String origin, ApiError expected) {
        assertThatThrownBy(() -> validator.validateAndBuild(req, origin))
                .isInstanceOf(ValidationException.class)
                .extracting(ex -> ((ValidationException) ex).error())
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("Requisicao valida produz comando normalizado")
    void buildsCommandFromValidRequest() {
        SpeedReadingCommand command = validator.validateAndBuild(validRequest(), "FIXED");

        assertThat(command.licensePlate()).isEqualTo("ABC1D23");
        assertThat(command.measuredSpeed()).isEqualTo(92);
        assertThat(command.speedLimit()).isEqualTo(60);
        assertThat(command.equipmentId()).isEqualTo("RAD-CWB-001");
        assertThat(command.captureTimestamp()).isEqualTo(Instant.parse("2026-06-08T14:30:00Z"));
        assertThat(command.origin()).isEqualTo(CaptureOrigin.FIXED);
    }

    @Nested
    @DisplayName("Placa")
    class Plate {

        @ParameterizedTest
        @ValueSource(strings = {"ABC1234", "ABC1D23", "XYZ9999", "QWE2R34"})
        @DisplayName("Aceita formato antigo e Mercosul")
        void acceptsValidFormats(String plate) {
            assertThat(SpeedReadingValidator.isValidPlate(plate)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc1234", "AB1234", "ABCD234", "ABC123", "ABC12345",
                "ABC1234 ", "ABC-1234", "1BC1234", "ABC1DD3"})
        @DisplayName("Rejeita formatos invalidos (inclui minusculas e espacos)")
        void rejectsInvalidFormats(String plate) {
            assertThat(SpeedReadingValidator.isValidPlate(plate)).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Placa ausente ou vazia -> INVALID_LICENSE_PLATE")
        void missingPlate(String plate) {
            assertError(request(plate, 92.0, 60.0, "RAD-1", "2026-06-08T14:30:00Z"),
                    "FIXED", ApiError.INVALID_LICENSE_PLATE);
        }
    }

    @Nested
    @DisplayName("Velocidades")
    class Speeds {

        @ParameterizedTest
        @ValueSource(doubles = {0, -1, -50})
        @DisplayName("measuredSpeed <= 0 -> INVALID_MEASURED_SPEED")
        void invalidMeasuredSpeed(double measured) {
            assertError(request("ABC1234", measured, 60.0, "RAD-1", "2026-06-08T14:30:00Z"),
                    "FIXED", ApiError.INVALID_MEASURED_SPEED);
        }

        @Test
        @DisplayName("measuredSpeed decimal positivo e aceito e arredondado")
        void acceptsDecimalMeasuredSpeed() {
            SpeedReadingCommand cmd = validator.validateAndBuild(
                    request("ABC1234", 85.5, 60.0, "RAD-1", "2026-06-08T14:30:00Z"), "FIXED");
            assertThat(cmd.measuredSpeed()).isEqualTo(86);
        }

        @Test
        @DisplayName("measuredSpeed ausente -> INVALID_MEASURED_SPEED")
        void missingMeasuredSpeed() {
            assertError(request("ABC1234", null, 60.0, "RAD-1", "2026-06-08T14:30:00Z"),
                    "FIXED", ApiError.INVALID_MEASURED_SPEED);
        }

        @ParameterizedTest
        @ValueSource(doubles = {0, -10})
        @DisplayName("speedLimit <= 0 -> INVALID_SPEED_LIMIT")
        void invalidSpeedLimit(double limit) {
            assertError(request("ABC1234", 92.0, limit, "RAD-1", "2026-06-08T14:30:00Z"),
                    "FIXED", ApiError.INVALID_SPEED_LIMIT);
        }
    }

    @Nested
    @DisplayName("EquipmentId")
    class EquipmentId {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("equipmentId ausente/em branco -> INVALID_EQUIPMENT_ID")
        void blankEquipmentId(String equipmentId) {
            assertError(request("ABC1234", 92.0, 60.0, equipmentId, "2026-06-08T14:30:00Z"),
                    "FIXED", ApiError.INVALID_EQUIPMENT_ID);
        }
    }

    @Nested
    @DisplayName("captureTimestamp")
    class CaptureTimestamp {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"08/06/2026", "2026-06-08", "not-a-date", "2026-13-08T14:30:00Z"})
        @DisplayName("Ausente ou fora do ISO-8601 -> INVALID_CAPTURE_TIMESTAMP")
        void invalidTimestamp(String timestamp) {
            assertError(request("ABC1234", 92.0, 60.0, "RAD-1", timestamp),
                    "FIXED", ApiError.INVALID_CAPTURE_TIMESTAMP);
        }

        @Test
        @DisplayName("Timestamp no futuro -> INVALID_CAPTURE_TIMESTAMP")
        void futureTimestamp() {
            assertError(request("ABC1234", 92.0, 60.0, "RAD-1", "2099-01-01T00:00:00Z"),
                    "FIXED", ApiError.INVALID_CAPTURE_TIMESTAMP);
        }

        @Test
        @DisplayName("Aceita offset diferente de Z")
        void acceptsNonZuluOffset() {
            SpeedReadingCommand command = validator.validateAndBuild(
                    request("ABC1234", 92.0, 60.0, "RAD-1", "2026-06-08T11:30:00-03:00"), "FIXED");
            assertThat(command.captureTimestamp()).isEqualTo(Instant.parse("2026-06-08T14:30:00Z"));
        }
    }

    @Nested
    @DisplayName("Header x-origin")
    class Origin {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"fixed", "Mobile", "CAR", "FIXED "})
        @DisplayName("Ausente ou diferente dos valores aceitos (case-sensitive) -> INVALID_ORIGIN")
        void invalidOrigin(String origin) {
            assertError(validRequest(), origin, ApiError.INVALID_ORIGIN);
        }

        @ParameterizedTest
        @ValueSource(strings = {"FIXED", "MOBILE", "HANDHELD"})
        @DisplayName("Aceita os tres valores validos")
        void validOrigins(String origin) {
            SpeedReadingCommand command = validator.validateAndBuild(validRequest(), origin);
            assertThat(command.origin()).isEqualTo(CaptureOrigin.valueOf(origin));
        }
    }
}
