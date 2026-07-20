package com.nexthome.backend.alert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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
    private Integer targetGrade;
    @Column(name = "target_gap_percent")
    private BigDecimal targetGapPercent;
    @Column(name = "historical_gap_percentile")
    private BigDecimal historicalGapPercentile;
    @Column(nullable = false)
    private boolean enabled = true;

    protected AlertCondition() {
    }

    AlertCondition(AlertConditionRequest request) {
        browserId = request.browserId();
        currentRegionId = request.currentRegionId();
        targetRegionId = request.targetRegionId();
        targetGrade = request.targetGrade();
        targetGapPercent = request.targetGapPercent();
        historicalGapPercentile = request.historicalGapPercentile();
    }

    AlertConditionResponse response() {
        return new AlertConditionResponse(id, browserId, currentRegionId, targetRegionId, targetGrade,
                targetGapPercent, historicalGapPercentile, enabled);
    }
}
