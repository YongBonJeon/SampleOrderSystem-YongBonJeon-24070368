package org.example.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
