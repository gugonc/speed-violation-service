package com.velsis.speedviolation;

import com.velsis.speedviolation.config.ToleranceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ToleranceProperties.class)
public class SpeedViolationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpeedViolationServiceApplication.class, args);
    }
}
