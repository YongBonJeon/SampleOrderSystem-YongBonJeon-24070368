package org.example.view;

import java.util.Scanner;

public class ApprovalInputView {

    private final Scanner scanner;

    public ApprovalInputView(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readOrderId() {
        System.out.print("처리할 주문번호 > ");
        return scanner.nextLine().trim();
    }

    public String readDecision() {
        System.out.print("승인(Y) / 거절(N) > ");
        return scanner.nextLine().trim().toUpperCase();
    }
}
