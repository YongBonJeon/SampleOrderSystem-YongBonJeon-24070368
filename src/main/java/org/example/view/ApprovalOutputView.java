package org.example.view;

import org.example.model.Order;
import org.example.model.ProductionJob;

import java.io.PrintStream;
import java.util.List;

public class ApprovalOutputView {

    private final PrintStream out;

    public ApprovalOutputView(PrintStream out) {
        this.out = out;
    }

    public void showReservedList(List<Order> orders) {
        out.printf("%n대기 주문 목록 (총 %d건)%n", orders.size());
        out.println("--------------------------------------------------------------");
        out.println(" 주문번호              시료 ID   고객명           수량");
        for (Order o : orders) {
            out.printf(" %-22s %-9s %-16s %d ea%n",
                    o.getOrderId(), o.getSampleId(), o.getCustomerName(), o.getQuantity());
        }
        out.println("--------------------------------------------------------------");
    }

    public void showNoReservedOrders() {
        out.println("대기 중인 주문이 없습니다.");
    }

    public void showOrderNotFound(String orderId) {
        out.printf("[ERROR] 존재하지 않는 주문번호입니다: %s%n", orderId);
    }

    public void showOrderNotReserved(String orderId) {
        out.printf("[ERROR] RESERVED 상태가 아닌 주문입니다: %s%n", orderId);
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
}
