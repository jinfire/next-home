package com.nexthome.backend.alert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alert_condition")
class AlertCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "browser_id", nullable = false)
    private UUID browserId;
    @Column(name = "current_region_id", nullable = false)
    private long currentRegionId;
    @Column(name = "target_region_id")
    private Long targetRegionId;
    @Column(name = "target_grade")
    private Short targetGrade;
    @Column(name = "target_gap_percent")
    private BigDecimal targetGapPercent;
    @Column(name = "historical_gap_percentile")
    private BigDecimal historicalGapPercentile;
    @Column(nullable = false)
    private boolean enabled = true;
    @Column(name = "last_triggered_at")
    private Instant lastTriggeredAt;

    protected AlertCondition() {
    }

    AlertCondition(AlertConditionRequest request) {
        update(request);
    }

    void update(AlertConditionRequest request) {
        browserId = request.browserId();
        currentRegionId = request.currentRegionId();
        targetRegionId = request.targetRegionId();
        targetGrade = request.targetGrade() == null ? null : request.targetGrade().shortValue();
        targetGapPercent = request.targetGapPercent();
        historicalGapPercentile = request.historicalGapPercentile();
        enabled = true;
        lastTriggeredAt = null;
    }

    boolean sameTarget(AlertConditionRequest request) {
        Integer grade = targetGrade == null ? null : targetGrade.intValue();
        return currentRegionId == request.currentRegionId()
                && java.util.Objects.equals(targetRegionId, request.targetRegionId())
                && java.util.Objects.equals(grade, request.targetGrade());
    }

    AlertConditionResponse response() {
        return new AlertConditionResponse(id, browserId, currentRegionId, targetRegionId,
                targetGrade == null ? null : targetGrade.intValue(),
                targetGapPercent, historicalGapPercentile, enabled);
    }

    UUID browserId() {
        return browserId;
    }

    long currentRegionId() {
        return currentRegionId;
    }

    Long targetRegionId() {
        return targetRegionId;
    }

    Integer targetGrade() {
        return targetGrade == null ? null : targetGrade.intValue();
    }

    AlertThresholds thresholds() {
        return new AlertThresholds(targetGapPercent, historicalGapPercentile);
    }

    boolean canTrigger(Instant now, Duration cooldown) {
        return lastTriggeredAt == null || lastTriggeredAt.plus(cooldown).compareTo(now) <= 0;
    }

    void markTriggered(Instant now) {
        lastTriggeredAt = now;
    }
}
