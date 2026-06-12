package org.example.view;

import org.example.model.Order;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReleaseOutputView {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final PrintStream out;

    public ReleaseOutputView(PrintStream out) {
        this.out = out;
    }

    public void showConfirmedList(List<Order> orders) {
        out.println("\n=== 출고 처리 ===");
        out.println("\n[출고 가능 주문 (CONFIRMED)]");
        out.printf("  %s  %s  %s  %s%n",
                padRight("번호", 4),
                padRight("주문번호", 22),
                padRight("고객", 14),
                "수량");
        for (int i = 0; i < orders.size(); i++) {
            Order o = orders.get(i);
            out.printf("  %s  %s  %s  %d ea%n",
                    padRight("[" + (i + 1) + "]", 4),
                    padRight(o.getOrderId(), 22),
                    padRight(o.getCustomerName(), 14),
                    o.getQuantity());
        }
        out.print("\n출고할 번호 (0: 취소) > ");
    }

    public void showNoConfirmedOrders() {
        out.println("\n=== 출고 처리 ===");
        out.println("출고 대기 중인 주문이 없습니다.");
    }

    public void showReleased(Order order, LocalDateTime processedAt) {
        out.println("\n출고 처리 완료.");
        out.printf("  주문번호  %s%n", order.getOrderId());
        out.printf("  출고수량  %d ea%n", order.getQuantity());
        out.printf("  처리일시  %s%n", processedAt.format(DT_FMT));
        out.println("  상태      CONFIRMED -> RELEASE");
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
