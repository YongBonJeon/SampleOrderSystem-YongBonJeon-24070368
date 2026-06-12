package org.example.view;

import org.example.model.Order;
import org.example.model.Sample;

import java.io.PrintStream;

public class OrderOutputView {

    private final PrintStream out;

    public OrderOutputView(PrintStream out) {
        this.out = out;
    }

    public void showOrderConfirm(Sample sample, String customerName, int quantity) {
        out.println("\n[주문 확인]");
        out.printf("  시료     : %s %s%n", sample.getId(), sample.getName());
        out.printf("  고객명   : %s%n", customerName);
        out.printf("  주문 수량: %d ea%n", quantity);
    }

    public void showOrderComplete(Order order) {
        out.println("\n주문 접수 완료");
        out.printf("  주문번호 : %s%n", order.getOrderId());
        out.printf("  상태     : %s%n", order.getStatus());
    }

    public void showOrderCancelled() {
        out.println("주문이 취소되었습니다.");
    }

    public void showSampleNotFound(String sampleId) {
        out.printf("[ERROR] 등록되지 않은 시료 ID입니다: %s%n", sampleId);
    }

    public void showError(String msg) {
        out.println("[ERROR] " + msg);
    }
}
