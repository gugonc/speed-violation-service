package com.velsis.speedviolation.infrastructure;

import com.velsis.speedviolation.domain.model.Severity;
import com.velsis.speedviolation.domain.model.Violation;
import com.velsis.speedviolation.domain.service.ViolationRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
@ConditionalOnExpression("!'${spring.datasource.url:}'.trim().isEmpty()")
public class JdbcViolationRepository implements ViolationRepository {

    private final JdbcTemplate jdbc;

    public JdbcViolationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(Violation v) {
        jdbc.update("""
                INSERT INTO violations
                  (license_plate, equipment_id, measured_speed, considered_speed,
                   speed_limit, excess_percentage, severity, capture_timestamp, processed_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                v.licensePlate(), v.equipmentId(), v.measuredSpeed(), v.consideredSpeed(),
                v.speedLimit(), v.excessPercentage(), v.severity().name(),
                Timestamp.from(v.captureTimestamp()), Timestamp.from(v.processedAt())
        );
    }

    @Override
    public List<Violation> findByLicensePlate(String licensePlate) {
        return jdbc.query("""
                SELECT license_plate, equipment_id, measured_speed, considered_speed,
                       speed_limit, excess_percentage, severity, capture_timestamp, processed_at
                FROM violations
                WHERE license_plate = ?
                ORDER BY processed_at DESC
                """,
                (rs, rowNum) -> new Violation(
                        rs.getString("license_plate"),
                        rs.getString("equipment_id"),
                        rs.getInt("measured_speed"),
                        rs.getInt("considered_speed"),
                        rs.getInt("speed_limit"),
                        rs.getDouble("excess_percentage"),
                        Severity.valueOf(rs.getString("severity")),
                        rs.getTimestamp("capture_timestamp").toInstant(),
                        rs.getTimestamp("processed_at").toInstant()
                ),
                licensePlate
        );
    }
}
