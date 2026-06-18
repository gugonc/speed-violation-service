package com.velsis.speedviolation.domain.service;

import com.velsis.speedviolation.config.ToleranceProperties;
import com.velsis.speedviolation.domain.model.EvaluationResult;
import com.velsis.speedviolation.domain.model.Severity;
import com.velsis.speedviolation.domain.model.SpeedReadingCommand;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;

@Service
public class SpeedEvaluationService {

    private static final int MEDIUM_MAX_PERCENT = 20;
    private static final int SERIOUS_MAX_PERCENT = 50;
    private static final int PERCENT_BASE = 100;

    private final ToleranceProperties tolerance;
    private final Clock clock;

    public SpeedEvaluationService(ToleranceProperties tolerance, Clock clock) {
        this.tolerance = tolerance;
        this.clock = clock;
    }

    public EvaluationResult evaluate(SpeedReadingCommand command) {
        int considered = applyTolerance(command.measuredSpeed(), command.speedLimit());
        Instant processedAt = clock.instant();

        if (considered <= command.speedLimit()) {
            return EvaluationResult.noViolation(command, considered, processedAt);
        }

        double excess = excessPercentage(considered, command.speedLimit());
        Severity severity = classify(considered, command.speedLimit());
        return EvaluationResult.withViolation(command, considered, excess, severity, processedAt);
    }

    int applyTolerance(int measuredSpeed, int speedLimit) {
        if (speedLimit <= tolerance.thresholdSpeed()) {
            return measuredSpeed - tolerance.kmh();
        }
        long retained = (long) measuredSpeed * (PERCENT_BASE - tolerance.percent());
        return (int) (retained / PERCENT_BASE);
    }

    double excessPercentage(int consideredSpeed, int speedLimit) {
        return BigDecimal.valueOf((long) (consideredSpeed - speedLimit) * PERCENT_BASE)
                .divide(BigDecimal.valueOf(speedLimit), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    Severity classify(int consideredSpeed, int speedLimit) {
        long excessScaled = (long) (consideredSpeed - speedLimit) * PERCENT_BASE;
        if (excessScaled <= (long) MEDIUM_MAX_PERCENT * speedLimit) {
            return Severity.MEDIUM;
        }
        if (excessScaled <= (long) SERIOUS_MAX_PERCENT * speedLimit) {
            return Severity.SERIOUS;
        }
        return Severity.VERY_SERIOUS;
    }
}
