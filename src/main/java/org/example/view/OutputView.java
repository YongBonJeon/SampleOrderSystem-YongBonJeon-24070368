package org.example.view;

import java.io.PrintStream;

public class OutputView {
    private final PrintStream out;

    public OutputView() {
        this.out = System.out;
    }

    public OutputView(PrintStream out) {
        this.out = out;
    }

    public void showMainMenu() {
        printDivider();
        out.println("  반도체 시료 생산주문관리 시스템");
        printDivider();
        out.println("  [1] 시료 관리       [2] 시료 주문");
        out.println("  [3] 주문 승인/거절  [4] 모니터링");
        out.println("  [5] 생산라인 조회   [6] 출고 처리");
        out.println("  [0] 종료");
        out.print("선택 > ");
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
