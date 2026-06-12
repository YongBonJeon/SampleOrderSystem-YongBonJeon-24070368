package org.example.view;

import java.io.InputStream;
import java.util.Scanner;

public class InputView {
    private final Scanner scanner;

    public InputView() {
        this.scanner = new Scanner(System.in);
    }

    public InputView(InputStream in) {
        this.scanner = new Scanner(in);
    }

    public String readLine() {
        return scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        return readLine();
    }

    public int readInt() {
        String line = readLine();
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
