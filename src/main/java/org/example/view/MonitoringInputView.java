package org.example.view;

import java.util.Scanner;

public class MonitoringInputView {
    private final Scanner scanner;
    public MonitoringInputView(Scanner scanner) { this.scanner = scanner; }
    public String readSubMenuSelection() { return scanner.nextLine().trim(); }
}
