package org.example.view;

import java.util.Scanner;

public class ApprovalInputView {

    private final Scanner scanner;

    public ApprovalInputView(Scanner scanner) {
        this.scanner = scanner;
    }

    public int readSelectionNumber() {
        System.out.print("승인할 번호 (0: 취소) > ");
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String readDecision() {
        System.out.print("승인(Y) / 거절(N) > ");
        return scanner.nextLine().trim().toUpperCase();
    }
}
