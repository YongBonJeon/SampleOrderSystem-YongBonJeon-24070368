package org.example.view;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OutputView {
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final PrintStream out;

    public OutputView() {
        this.out = System.out;
    }

    public OutputView(PrintStream out) {
        this.out = out;
    }

    public void showMainMenu(int sampleCount, int totalStock, int orderCount, int producingCount, LocalDateTime now) {
        printDivider();
        out.println("  반도체 시료 생산주문관리 시스템");
        printDivider();
        out.printf("  시스템 현황  %s%n", now.format(DT_FORMAT));
        out.println();
        out.printf("  등록 시료  %d종    총 재고  %,d ea%n", sampleCount, totalStock);
        out.printf("  전체 주문  %d건    생산라인  %d건 대기%n", orderCount, producingCount);
        out.println();
        out.println("  [1] 시료 관리       [2] 시료 주문");
        out.println("  [3] 주문 승인/거절  [4] 모니터링");
        out.println("  [5] 생산라인 조회   [6] 출고 처리");
        out.println("  [0] 종료");
        out.print("선택 > ");
    }

    public void showMainMenu() {
        showMainMenu(0, 0, 0, 0, LocalDateTime.now());
    }

    public void showNotImplemented() {
        out.println("[미구현] 준비 중입니다.");
    }

    public void showError(String msg) {
        out.println("[ERROR] " + msg);
    }

    public void println(String msg) {
        out.println(msg);
    }

    public void printDivider() {
        out.println("=".repeat(60));
    }
}
