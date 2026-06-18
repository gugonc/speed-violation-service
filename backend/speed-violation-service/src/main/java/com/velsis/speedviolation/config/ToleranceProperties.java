package com.velsis.speedviolation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "violation.tolerance")
public record ToleranceProperties(int kmh, int percent, int thresholdSpeed) {

    public ToleranceProperties {
        if (kmh < 0) {
            throw new IllegalArgumentException("violation.tolerance.kmh must be >= 0");
        }
        if (percent < 0 || percent >= 100) {
            throw new IllegalArgumentException("violation.tolerance.percent must be in [0, 100)");
        }
        if (thresholdSpeed <= 0) {
            throw new IllegalArgumentException("violation.tolerance.threshold-speed must be > 0");
        }
    }
}
