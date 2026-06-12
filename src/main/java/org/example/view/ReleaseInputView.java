package org.example.view;

import java.util.Scanner;

public class ReleaseInputView {

    private final Scanner scanner;

    public ReleaseInputView(Scanner scanner) {
        this.scanner = scanner;
    }

    public int readSelectionNumber() {
        String line = scanner.nextLine().trim();
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
