package com.velsis.speedviolation.domain.service;

import com.velsis.speedviolation.config.ToleranceProperties;
import com.velsis.speedviolation.domain.model.CaptureOrigin;
import com.velsis.speedviolation.domain.model.EvaluationResult;
import com.velsis.speedviolation.domain.model.Severity;
import com.velsis.speedviolation.domain.model.SpeedReadingCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class SpeedEvaluationServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-06-10T12:00:00Z");
    private static final Instant CAPTURE = Instant.parse("2026-06-08T14:30:00Z");

    private final ToleranceProperties tolerance = new ToleranceProperties(7, 7, 100);
    private final Clock clock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
    private final SpeedEvaluationService service = new SpeedEvaluationService(tolerance, clock);

    private SpeedReadingCommand command(int measured, int limit) {
        return new SpeedReadingCommand("ABC1D23", measured, limit, "RAD-CWB-001", CAPTURE, CaptureOrigin.FIXED);
    }

    @Nested
    @DisplayName("Velocidade considerada (tolerancia)")
    class ToleranceStage {

        @Test
        @DisplayName("Limite <= 100: subtrai tolerancia fixa em km/h")
        void appliesFixedToleranceBelowThreshold() {
            assertThat(service.applyTolerance(92, 60)).isEqualTo(85);
        }

        @Test
        @DisplayName("Limite exatamente 100: ainda usa tolerancia fixa")
        void thresholdBoundaryUsesFixedTolerance() {
            assertThat(service.applyTolerance(120, 100)).isEqualTo(113);
        }

        @Test
        @DisplayName("Limite > 100: subtrai tolerancia percentual truncada")
        void appliesPercentToleranceAboveThreshold() {
            // 120 * (100 - 7) / 100 = 11160 / 100 = 111 (truncado)
            assertThat(service.applyTolerance(120, 110)).isEqualTo(111);
        }

        @Test
        @DisplayName("Tolerancia percentual trunca, nao arredonda")
        void percentToleranceTruncates() {
            // 130 * 93 / 100 = 120.9 -> 120
            assertThat(service.applyTolerance(130, 120)).isEqualTo(120);
        }
    }

    @Nested
    @DisplayName("Apuracao completa")
    class FullEvaluation {

        @Test
        @DisplayName("Exemplo do enunciado: 92 em via de 60 -> SERIOUS 218-II, 41.67%")
        void documentExampleSerious() {
            EvaluationResult result = service.evaluate(command(92, 60));

            assertThat(result.hasViolation()).isTrue();
            assertThat(result.consideredSpeed()).isEqualTo(85);
            assertThat(result.excessPercentage()).isEqualTo(41.67);
            assertThat(result.severity()).isEqualTo(Severity.SERIOUS);
            assertThat(result.severity().ctbCode()).isEqualTo("218-II");
            assertThat(result.processedAt()).isEqualTo(FIXED_NOW);
        }

        @Test
        @DisplayName("Velocidade considerada igual ao limite -> sem infracao")
        void consideredEqualsLimitIsNoViolation() {
            EvaluationResult result = service.evaluate(command(67, 60)); // 67-7=60

            assertThat(result.hasViolation()).isFalse();
            assertThat(result.consideredSpeed()).isEqualTo(60);
            assertThat(result.excessPercentage()).isEqualTo(0.0);
            assertThat(result.severity()).isNull();
        }

        @Test
        @DisplayName("Velocidade dentro da margem de tolerancia -> sem infracao")
        void withinToleranceIsNoViolation() {
            EvaluationResult result = service.evaluate(command(64, 60)); // 64-7=57 <= 60

            assertThat(result.hasViolation()).isFalse();
            assertThat(result.consideredSpeed()).isEqualTo(57);
        }
    }

    @Nested
    @DisplayName("Classificacao por gravidade e valores de fronteira")
    class SeverityClassification {

        @Test
        @DisplayName("Exatamente 20% -> MEDIUM (fronteira inclusiva)")
        void exactly20PercentIsMedium() {
            // considerada 72, limite 60 -> (72-60)/60 = 20%
            EvaluationResult result = service.evaluate(command(79, 60)); // 79-7=72
            assertThat(result.consideredSpeed()).isEqualTo(72);
            assertThat(result.excessPercentage()).isEqualTo(20.0);
            assertThat(result.severity()).isEqualTo(Severity.MEDIUM);
        }

        @Test
        @DisplayName("Logo acima de 20% -> SERIOUS")
        void justAbove20PercentIsSerious() {
            // considerada 73, limite 60 -> 21.67%
            EvaluationResult result = service.evaluate(command(80, 60)); // 80-7=73
            assertThat(result.severity()).isEqualTo(Severity.SERIOUS);
        }

        @Test
        @DisplayName("Exatamente 50% -> SERIOUS (fronteira inclusiva)")
        void exactly50PercentIsSerious() {
            // considerada 90, limite 60 -> 50%
            EvaluationResult result = service.evaluate(command(97, 60)); // 97-7=90
            assertThat(result.consideredSpeed()).isEqualTo(90);
            assertThat(result.excessPercentage()).isEqualTo(50.0);
            assertThat(result.severity()).isEqualTo(Severity.SERIOUS);
        }

        @Test
        @DisplayName("Logo acima de 50% -> VERY_SERIOUS")
        void justAbove50PercentIsVerySerious() {
            // considerada 91, limite 60 -> 51.67%
            EvaluationResult result = service.evaluate(command(98, 60)); // 98-7=91
            assertThat(result.severity()).isEqualTo(Severity.VERY_SERIOUS);
        }

        @ParameterizedTest(name = "considerada {0}, limite {1} -> {2}")
        @CsvSource({
                "66, 60, MEDIUM",          // 10%
                "72, 60, MEDIUM",          // 20% exato
                "75, 60, SERIOUS",         // 25%
                "90, 60, SERIOUS",         // 50% exato
                "100, 60, VERY_SERIOUS"    // 66.67%
        })
        @DisplayName("Tabela de fronteiras de classificacao")
        void classificationTable(int considered, int limit, Severity expected) {
            assertThat(service.classify(considered, limit)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Percentual de excesso")
    class ExcessPercentage {

        @Test
        @DisplayName("Arredonda para 2 casas com HALF_UP")
        void roundsToTwoDecimals() {
            // (85-60)/60 * 100 = 41.6666... -> 41.67
            assertThat(service.excessPercentage(85, 60)).isEqualTo(41.67);
        }

        @Test
        @DisplayName("Via com limite acima de 100 km/h usa tolerancia percentual")
        void highSpeedRoadUsesPercentTolerance() {
            // limite 110: considerada = 130*93/100 = 120; excesso = (120-110)/110 = 9.09%
            EvaluationResult result = service.evaluate(command(130, 110));
            assertThat(result.consideredSpeed()).isEqualTo(120);
            assertThat(result.excessPercentage()).isEqualTo(9.09);
            assertThat(result.severity()).isEqualTo(Severity.MEDIUM);
        }
    }
}
