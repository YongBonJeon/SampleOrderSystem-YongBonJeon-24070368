package org.example.view;

import java.util.Scanner;

public class OrderInputView {

    private final Scanner scanner;

    public OrderInputView(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readSampleId() {
        System.out.print("시료 ID    > ");
        return scanner.nextLine().trim();
    }

    public String readCustomerName() {
        System.out.print("고객명     > ");
        return scanner.nextLine().trim();
    }

    public int readQuantity() {
        while (true) {
            System.out.print("주문 수량  > ");
            String line = scanner.nextLine().trim();
            try {
                int qty = Integer.parseInt(line);
                if (qty >= 1) return qty;
            } catch (NumberFormatException ignored) {}
            System.out.println("[ERROR] 1 이상의 정수를 입력하세요.");
        }
    }

    public String readConfirm() {
        System.out.print("주문하시겠습니까? (Y/N) > ");
        return scanner.nextLine().trim().toUpperCase();
    }
}
