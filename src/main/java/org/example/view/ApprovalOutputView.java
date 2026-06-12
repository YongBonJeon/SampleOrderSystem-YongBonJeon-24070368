package org.example.view;

import org.example.model.Order;
import org.example.model.ProductionJob;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

public class ApprovalOutputView {

    private final PrintStream out;

    public ApprovalOutputView(PrintStream out) {
        this.out = out;
    }

    public void showReservedList(List<Order> orders, Map<String, String> sampleNames) {
        out.printf("%n승인 대기 중인 예약 목록  (RESERVED)%n");
        out.println("------------------------------------------------------------------------");
        out.println(" 번호  주문번호              고객              시료                수량     상태");
        int i = 1;
        for (Order o : orders) {
            String sampleName = sampleNames.getOrDefault(o.getSampleId(), o.getSampleId());
            out.printf(" [%d]   %-22s %s %s %d ea   %s%n",
                    i++,
                    o.getOrderId(),
                    padRight(o.getCustomerName(), 16),
                    padRight(sampleName, 20),
                    o.getQuantity(),
                    o.getStatus());
        }
        out.println("------------------------------------------------------------------------");
    }

    public void showNoReservedOrders() {
        out.println("대기 중인 주문이 없습니다.");
    }

    public void showInvalidSelection() {
        out.println("[ERROR] 올바른 번호를 입력하세요.");
    }

    public void showStockInfo(int stock, int orderQty) {
        String status = stock >= orderQty ? "재고 충분" : "재고 부족";
        out.printf("재고 현황: 현재 재고 %d ea  /  주문 수량 %d ea  →  %s%n", stock, orderQty, status);
    }

    public void showConfirmed(Order order) {
        out.printf("주문 %s 승인 완료 → CONFIRMED%n", order.getOrderId());
    }

    public void showProducing(Order order, ProductionJob job) {
        out.printf("생산 등록: 실 생산량 %d ea / 총 생산시간 %.1f min%n",
                job.getActualQty(), job.getTotalTime());
        out.printf("주문 %s 승인 완료 → PRODUCING%n", order.getOrderId());
    }

    public void showRejected(Order order) {
        out.printf("주문 %s 거절 완료 → REJECTED%n", order.getOrderId());
    }

    private static String padRight(String s, int targetWidth) {
        int padding = Math.max(0, targetWidth - displayWidth(s));
        return s + " ".repeat(padding);
    }

    private static int displayWidth(String s) {
        int w = 0;
        for (char c : s.toCharArray()) w += isWideChar(c) ? 2 : 1;
        return w;
    }

    private static boolean isWideChar(char c) {
        return (c >= '가' && c <= '힣')
            || (c >= 'ᄀ' && c <= 'ᇿ')
            || (c >= '　' && c <= '鿿')
            || (c >= '豈' && c <= '﫿')
            || (c >= '！' && c <= '｠')
            || (c >= '￠' && c <= '￦');
    }
}
