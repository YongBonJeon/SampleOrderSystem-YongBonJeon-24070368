package org.example.view;

import org.example.model.Sample;

import java.io.PrintStream;
import java.util.List;

public class SampleOutputView {
    private static final String SEPARATOR = "------------------------------------------------------------";

    private final PrintStream out;

    public SampleOutputView(PrintStream out) {
        this.out = out;
    }

    public void showMenu() {
        out.println("\n=== 시료 관리 ===");
        out.println("[1] 시료 등록");
        out.println("[2] 전체 목록");
        out.println("[3] 이름 검색");
        out.println("[0] 메인 메뉴로");
        out.print("> ");
    }

    public void showSampleList(List<Sample> list) {
        if (list.isEmpty()) {
            out.println("등록된 시료가 없습니다.");
            return;
        }
        out.printf("%n등록 시료 목록 (총 %d종)%n", list.size());
        out.println(SEPARATOR);
        out.printf(" %-8s %-22s %-14s %-8s %s%n", "ID", "시료명", "평균생산시간", "수율", "현재재고");
        for (Sample s : list) {
            out.printf(" %-8s %-22s %.1f min/ea   %.2f   %d ea%n",
                    s.getId(), s.getName(), s.getAvgProductionTime(), s.getYieldRate(), s.getStock());
        }
        out.println(SEPARATOR);
    }

    public void showRegisterConfirm(Sample sample) {
        out.printf("%n[등록 확인]%n  ID: %s / 이름: %s / 생산시간: %.1f min/ea / 수율: %.2f / 재고: %d ea%n",
                sample.getId(), sample.getName(),
                sample.getAvgProductionTime(), sample.getYieldRate(), sample.getStock());
        out.print("등록하시겠습니까? (Y/N) > ");
    }

    public void showRegisterSuccess(Sample sample) {
        out.printf("등록 완료: %s %s%n", sample.getId(), sample.getName());
    }

    public void showNotFound() {
        out.println("검색 결과가 없습니다.");
    }

    public void showError(String msg) {
        out.println("[ERROR] " + msg);
    }

    public void promptId()               { out.print("시료 ID       > "); }
    public void promptName()             { out.print("시료명        > "); }
    public void promptAvgProductionTime(){ out.print("평균 생산시간 > "); }
    public void promptYieldRate()        { out.print("수율          > "); }
    public void promptStock()            { out.print("초기 재고     > "); }
    public void promptSearchKeyword()    { out.print("검색어 > "); }
}
