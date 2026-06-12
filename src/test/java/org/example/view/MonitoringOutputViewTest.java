package org.example.view;

import org.example.model.OrderStatus;
import org.example.model.Sample;
import org.example.model.StockLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MonitoringOutputViewTest {

    private record Context(MonitoringOutputView view, ByteArrayOutputStream output) {}

    private Context buildContext() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        return new Context(new MonitoringOutputView(new PrintStream(baos, true, StandardCharsets.UTF_8)), baos);
    }

    private String output(Context ctx) {
        return ctx.output().toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("showOrderCounts 출력에 상태명과 건수가 포함된다")
    void showOrderCounts_containsStatusAndCount() {
        Context ctx = buildContext();
        ctx.view().showOrderCounts(Map.of(
                OrderStatus.RESERVED, 3L,
                OrderStatus.CONFIRMED, 8L,
                OrderStatus.PRODUCING, 3L,
                OrderStatus.RELEASE, 18L
        ));

        String out = output(ctx);
        assertTrue(out.contains("RESERVED"));
        assertTrue(out.contains("3건"));
        assertTrue(out.contains("CONFIRMED"));
        assertTrue(out.contains("8건"));
    }

    @Test
    @DisplayName("showStockStatus 출력에 시료명·재고 상태·잔여율이 포함된다")
    void showStockStatus_containsStockLevelAndRate() {
        Sample sample = new Sample("S-001", "SiC 파워기판-6인치", 0.8, 0.92, 30);
        Context ctx = buildContext();

        ctx.view().showStockStatus(
                List.of(sample),
                Map.of("S-001", StockLevel.SHORTAGE),
                Map.of("S-001", 6.0)
        );

        String out = output(ctx);
        assertTrue(out.contains("SiC 파워기판-6인치"));
        assertTrue(out.contains("부족"));
        assertTrue(out.contains("6"));  // 잔여율
    }
}
