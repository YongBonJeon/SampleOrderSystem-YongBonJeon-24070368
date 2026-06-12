package org.example.view;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.model.Sample;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderOutputViewTest {

    private record Context(OrderOutputView view, ByteArrayOutputStream output) {}

    private Context buildContext() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        return new Context(new OrderOutputView(new PrintStream(baos, true, StandardCharsets.UTF_8)), baos);
    }

    private String output(Context ctx) {
        return ctx.output().toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("접수 완료 메시지에 주문번호와 RESERVED 상태가 포함된다")
    void showOrderComplete_containsOrderIdAndStatus() {
        Order order = new Order("ORD-20260612-0001", "S-001", "한국반도체연구소", 200, OrderStatus.RESERVED);
        Context ctx = buildContext();

        ctx.view().showOrderComplete(order);

        String out = output(ctx);
        assertTrue(out.contains("ORD-20260612-0001"));
        assertTrue(out.contains("RESERVED"));
    }

    @Test
    @DisplayName("주문 확인 화면에 시료명·고객명·수량이 포함된다")
    void showOrderConfirm_containsSampleAndQuantity() {
        Sample sample = new Sample("S-001", "Silicon-Wafer", 0.5, 0.9, 100);
        Context ctx = buildContext();

        ctx.view().showOrderConfirm(sample, "한국반도체연구소", 200);

        String out = output(ctx);
        assertTrue(out.contains("Silicon-Wafer"));
        assertTrue(out.contains("한국반도체연구소"));
        assertTrue(out.contains("200"));
    }
}
