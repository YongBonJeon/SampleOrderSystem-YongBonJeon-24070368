package org.example.view;

import org.example.model.ProductionJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductionLineOutputViewTest {

    private record Context(ProductionLineOutputView view, ByteArrayOutputStream output) {}

    private Context buildContext() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        return new Context(new ProductionLineOutputView(new PrintStream(baos, true, StandardCharsets.UTF_8)), baos);
    }

    private String output(Context ctx) {
        return ctx.output().toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("현재 작업 출력에 주문번호·실 생산량·진행률이 포함된다")
    void showProductionLine_containsCurrentJobInfo() {
        ProductionJob current = new ProductionJob("ORD-20260612-0001", "S-001", 170, 0.9, 0.5);
        // totalTime = 105.0 min, startedAt = 09:00, now = 09:50 (50 min elapsed → ~47%)
        LocalDateTime startedAt = LocalDateTime.of(2026, 6, 12, 9, 0);
        current.setStartedAt(startedAt);
        LocalDateTime now = startedAt.plusMinutes(50);

        Context ctx = buildContext();
        ctx.view().showProductionLine(current, List.of(), now);

        String out = output(ctx);
        assertTrue(out.contains("ORD-20260612-0001"));
        assertTrue(out.contains("210"));   // actualQty
        assertTrue(out.contains("%"));     // progress percentage
    }

    @Test
    @DisplayName("완료 메시지에 주문번호와 재고 증가량이 포함된다")
    void showCompleted_containsOrderIdAndStock() {
        ProductionJob job = new ProductionJob("ORD-20260612-0001", "S-001", 170, 0.9, 0.5);
        Context ctx = buildContext();

        ctx.view().showCompleted(job, 240);

        String out = output(ctx);
        assertTrue(out.contains("ORD-20260612-0001"));
        assertTrue(out.contains("210")); // actualQty
    }
}
