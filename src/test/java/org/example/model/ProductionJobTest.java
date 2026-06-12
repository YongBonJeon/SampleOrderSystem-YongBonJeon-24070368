package org.example.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProductionJobTest {

    @Test
    @DisplayName("실 생산량은 ceil(부족분 / (수율 × 0.9)) 공식으로 계산된다")
    void actualQty_calculatedByCeil() {
        // ceil(170 / (0.9 × 0.9)) = ceil(209.876...) = 210
        ProductionJob job = new ProductionJob("ORD-20260612-0001", "S-001", 170, 0.9, 0.5);

        assertEquals(210, job.getActualQty());
    }

    @Test
    @DisplayName("총 생산시간은 평균 생산시간 × 실 생산량으로 계산된다")
    void totalTime_calculatedCorrectly() {
        // actualQty = 210, avgProductionTime = 0.5 → totalTime = 105.0
        ProductionJob job = new ProductionJob("ORD-20260612-0001", "S-001", 170, 0.9, 0.5);

        assertEquals(105.0, job.getTotalTime());
    }

    @Test
    @DisplayName("avgProductionTime이 getter로 조회된다")
    void avgProductionTime_storedCorrectly() {
        ProductionJob job = new ProductionJob("ORD-20260612-0001", "S-001", 170, 0.9, 0.5);

        assertEquals(0.5, job.getAvgProductionTime());
    }

    @Test
    @DisplayName("생성 직후 startedAt은 null이다")
    void startedAt_defaultIsNull() {
        ProductionJob job = new ProductionJob("ORD-20260612-0001", "S-001", 170, 0.9, 0.5);

        assertNull(job.getStartedAt());
    }

    @Test
    @DisplayName("setStartedAt 호출 후 getStartedAt이 동일 값을 반환한다")
    void setStartedAt_updatesField() {
        ProductionJob job = new ProductionJob("ORD-20260612-0001", "S-001", 170, 0.9, 0.5);
        LocalDateTime now = LocalDateTime.of(2026, 6, 12, 9, 0);

        job.setStartedAt(now);

        assertEquals(now, job.getStartedAt());
    }
}
