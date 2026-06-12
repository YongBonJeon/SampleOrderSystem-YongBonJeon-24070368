package org.example.view;

import org.example.model.OrderStatus;
import org.example.model.Sample;
import org.example.model.StockLevel;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

public class MonitoringOutputView {

    private static final int BAR_WIDTH = 10;
    private final PrintStream out;

    public MonitoringOutputView(PrintStream out) {
        this.out = out;
    }

    public void showSubMenu() {
        out.println("\n=== 모니터링 ===");
        out.println("[1] 주문량 확인    [2] 재고량 확인    [0] 뒤로");
        out.print("선택 > ");
    }

    public void showOrderCounts(Map<OrderStatus, Long> counts) {
        out.println("\n[상태별 주문 현황]");
        out.printf("  RESERVED  : %3d건%n",  counts.getOrDefault(OrderStatus.RESERVED,  0L));
        out.printf("  PRODUCING : %3d건  <- 생산라인 대기%n", counts.getOrDefault(OrderStatus.PRODUCING, 0L));
        out.printf("  CONFIRMED : %3d건%n",  counts.getOrDefault(OrderStatus.CONFIRMED, 0L));
        out.printf("  RELEASE   : %3d건%n",  counts.getOrDefault(OrderStatus.RELEASE,   0L));
    }

    public void showStockStatus(List<Sample> samples,
                                Map<String, StockLevel> levels,
                                Map<String, Double> remainingRates) {
        out.println("\n[재고 현황]");
        out.printf("  %s  %8s  %s  %s%n",
                padRight("시료명", 24), "재고", padRight("상태", 6), "잔여율");
        for (Sample s : samples) {
            StockLevel level = levels.getOrDefault(s.getId(), StockLevel.SURPLUS);
            double rate = remainingRates.getOrDefault(s.getId(), 0.0);
            String levelLabel = switch (level) {
                case SURPLUS  -> "여유";
                case SHORTAGE -> "부족";
                case DEPLETED -> "고갈";
            };
            String bar = progressBar(rate);
            out.printf("  %s  %5d ea  %s  %s %3.0f%%%n",
                    padRight(s.getName(), 24), s.getStock(), padRight(levelLabel, 6), bar, rate);
        }
    }

    private String progressBar(double pct) {
        int filled = (int) (pct / 100.0 * BAR_WIDTH);
        return "[" + "#".repeat(filled) + "-".repeat(BAR_WIDTH - filled) + "]";
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
