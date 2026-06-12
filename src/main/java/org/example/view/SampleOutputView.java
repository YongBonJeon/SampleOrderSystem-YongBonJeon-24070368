package org.example.view;

import org.example.model.Sample;

import java.io.PrintStream;
import java.util.List;

public class SampleOutputView {
    private static final String SEPARATOR = "------------------------------------------------------------------------";

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
        out.printf(" %s %s %s %s %s%n",
                padRight("ID", 10),
                padRight("시료명", 24),
                padRight("평균생산시간", 14),
                padRight("수율", 8),
                "현재재고");
        for (Sample s : list) {
            out.printf(" %s %s %s %s %d ea%n",
                    padRight(s.getId(), 10),
                    padRight(s.getName(), 24),
                    padRight(String.format("%.1f min/ea", s.getAvgProductionTime()), 14),
                    padRight(String.format("%.2f", s.getYieldRate()), 8),
                    s.getStock());
        }
        out.println(SEPARATOR);
    }

    // 한글(2칸)을 고려한 display width 계산
    private static int displayWidth(String s) {
        int w = 0;
        for (char c : s.toCharArray()) {
            w += isWideChar(c) ? 2 : 1;
        }
        return w;
    }

    private static boolean isWideChar(char c) {
        return (c >= '가' && c <= '힣')   // 한글 음절
            || (c >= 'ᄀ' && c <= 'ᇿ')   // 한글 자모
            || (c >= '　' && c <= '鿿')   // CJK
            || (c >= '豈' && c <= '﫿')
            || (c >= '！' && c <= '｠')
            || (c >= '￠' && c <= '￦');
    }

    private static String padRight(String s, int targetWidth) {
        int padding = Math.max(0, targetWidth - displayWidth(s));
        return s + " ".repeat(padding);
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
