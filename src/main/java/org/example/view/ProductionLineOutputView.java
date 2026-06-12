package org.example.view;

import org.example.model.ProductionJob;

import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProductionLineOutputView {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final int BAR_WIDTH = 10;

    private final PrintStream out;

    public ProductionLineOutputView(PrintStream out) {
        this.out = out;
    }

    public void showProductionLine(ProductionJob current, List<ProductionJob> waiting, LocalDateTime now) {
        out.println("\n=== 생산라인 조회 ===");
        out.println("현재 상태: RUNNING");
        out.println("\n[현재 생산 중]");
        out.printf("  주문번호    : %s%n", current.getOrderId());
        out.printf("  시료 ID     : %s%n", current.getSampleId());
        out.printf("  실 생산량   : %d ea (%.2f min/ea)%n",
                current.getActualQty(), current.getAvgProductionTime());

        LocalDateTime estimatedEnd = null;
        if (current.getStartedAt() != null) {
            double elapsedMinutes = Duration.between(current.getStartedAt(), now).toSeconds() / 60.0;
            double progressPct = Math.min(100.0, elapsedMinutes / current.getTotalTime() * 100.0);
            int producedQty = Math.min(current.getActualQty(),
                    (int) (elapsedMinutes / current.getAvgProductionTime()));
            estimatedEnd = current.getStartedAt()
                    .plusSeconds((long) (current.getTotalTime() * 60));

            out.printf("  진행        : %s %.0f%%   완료 예정 %s%n",
                    progressBar(progressPct), progressPct, estimatedEnd.format(TIME_FMT));
            out.printf("  생산완료량  : %d / %d ea%n", producedQty, current.getActualQty());
        }

        if (!waiting.isEmpty()) {
            out.printf("%n[대기 큐] (%d건)%n", waiting.size());
            LocalDateTime prevEnd = estimatedEnd != null ? estimatedEnd : now;
            for (int i = 0; i < waiting.size(); i++) {
                ProductionJob w = waiting.get(i);
                LocalDateTime waitEnd = prevEnd.plusSeconds((long) (w.getTotalTime() * 60));
                out.printf("  %d. %s  %s  실 생산량: %d ea  예상 완료: %s%n",
                        i + 1, w.getOrderId(), w.getSampleId(), w.getActualQty(),
                        waitEnd.format(TIME_FMT));
                prevEnd = waitEnd;
            }
        }
    }

    public void showNoJobInProgress() {
        out.println("\n=== 생산라인 조회 ===");
        out.println("생산 중인 작업이 없습니다.");
    }

    public void showCompleted(ProductionJob job, int newStock) {
        out.printf("완료(자동): %s → CONFIRMED / %s 재고 +%d ea%n",
                job.getOrderId(), job.getSampleId(), job.getActualQty());
    }

    private String progressBar(double progressPct) {
        int filled = (int) (progressPct / 100.0 * BAR_WIDTH);
        return "[" + "#".repeat(filled) + "-".repeat(BAR_WIDTH - filled) + "]";
    }
}
