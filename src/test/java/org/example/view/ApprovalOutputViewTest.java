package org.example.view;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.model.ProductionJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ApprovalOutputViewTest {

    private record Context(ApprovalOutputView view, ByteArrayOutputStream output) {}

    private Context buildContext() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        return new Context(new ApprovalOutputView(new PrintStream(baos, true, StandardCharsets.UTF_8)), baos);
    }

    private String output(Context ctx) {
        return ctx.output().toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("RESERVED 목록 출력에 주문번호가 포함된다")
    void showReservedList_containsOrderIds() {
        List<Order> orders = List.of(
                new Order("ORD-20260612-0001", "S-001", "한국반도체연구소", 200, OrderStatus.RESERVED)
        );
        Context ctx = buildContext();

        ctx.view().showReservedList(orders);

        assertTrue(output(ctx).contains("ORD-20260612-0001"));
    }

    @Test
    @DisplayName("CONFIRMED 전환 결과 출력에 CONFIRMED가 포함된다")
    void showConfirmed_containsStatusConfirmed() {
        Order order = new Order("ORD-20260612-0001", "S-001", "한국반도체연구소", 200, OrderStatus.CONFIRMED);
        Context ctx = buildContext();

        ctx.view().showConfirmed(order);

        assertTrue(output(ctx).contains("CONFIRMED"));
    }

    @Test
    @DisplayName("PRODUCING 전환 결과 출력에 실 생산량과 총 생산시간이 포함된다")
    void showProducing_containsActualQtyAndTotalTime() {
        Order order = new Order("ORD-20260612-0001", "S-001", "한국반도체연구소", 200, OrderStatus.PRODUCING);
        ProductionJob job = new ProductionJob("ORD-20260612-0001", "S-001", 170, 0.9, 0.5);
        Context ctx = buildContext();

        ctx.view().showProducing(order, job);

        String out = output(ctx);
        assertTrue(out.contains("210"));   // actualQty
        assertTrue(out.contains("105.0")); // totalTime
    }
}
