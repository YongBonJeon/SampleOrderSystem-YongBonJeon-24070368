package org.example.view;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleaseOutputViewTest {

    private record Context(ReleaseOutputView view, ByteArrayOutputStream output) {}

    private Context buildContext() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        return new Context(new ReleaseOutputView(new PrintStream(baos, true, StandardCharsets.UTF_8)), baos);
    }

    private String output(Context ctx) {
        return ctx.output().toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("showConfirmedList 출력에 번호·주문번호·수량이 포함된다")
    void showConfirmedList_containsOrderInfo() {
        Order order = new Order("ORD-20260612-0001", "S-001", "SK하이닉스", 150, OrderStatus.CONFIRMED);
        Context ctx = buildContext();

        ctx.view().showConfirmedList(List.of(order));

        String out = output(ctx);
        assertTrue(out.contains("1"));
        assertTrue(out.contains("ORD-20260612-0001"));
        assertTrue(out.contains("150"));
    }

    @Test
    @DisplayName("showReleased 출력에 주문번호·출고수량·처리일시가 포함된다")
    void showReleased_containsOrderInfoAndProcessedAt() {
        Order order = new Order("ORD-20260612-0001", "S-001", "SK하이닉스", 150, OrderStatus.CONFIRMED);
        LocalDateTime processedAt = LocalDateTime.of(2026, 6, 12, 9, 34, 2);
        Context ctx = buildContext();

        ctx.view().showReleased(order, processedAt);

        String out = output(ctx);
        assertTrue(out.contains("ORD-20260612-0001"));
        assertTrue(out.contains("150"));
        assertTrue(out.contains("09:34"));
    }
}
