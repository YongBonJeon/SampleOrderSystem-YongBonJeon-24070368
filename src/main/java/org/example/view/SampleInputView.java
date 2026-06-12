package org.example.view;

import java.util.Scanner;

public class SampleInputView {
    private final Scanner scanner;

    public SampleInputView(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readLine() {
        return scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    }

    public String readId()               { return readLine(); }
    public String readName()             { return readLine(); }
    public String readSearchKeyword()    { return readLine(); }

    public double readAvgProductionTime() {
        try { return Double.parseDouble(readLine()); } catch (NumberFormatException e) { return -1; }
    }

    public double readYieldRate() {
        try { return Double.parseDouble(readLine()); } catch (NumberFormatException e) { return -1; }
    }

    public int readStock() {
        try { return Integer.parseInt(readLine()); } catch (NumberFormatException e) { return -1; }
    }
}
