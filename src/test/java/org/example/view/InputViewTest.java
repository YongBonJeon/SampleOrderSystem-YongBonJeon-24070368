package org.example.view;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

class InputViewTest {

    @Test
    void readInt_withValidInput_returnsNumber() {
        InputView inputView = new InputView(new ByteArrayInputStream("3\n".getBytes()));

        int result = inputView.readInt();

        assertEquals(3, result);
    }

    @Test
    void readInt_withNonNumericInput_returnsMinusOne() {
        InputView inputView = new InputView(new ByteArrayInputStream("abc\n".getBytes()));

        int result = inputView.readInt();

        assertEquals(-1, result);
    }

    @Test
    void readLine_returnsInputLine() {
        InputView inputView = new InputView(new ByteArrayInputStream("hello\n".getBytes()));

        String result = inputView.readLine();

        assertEquals("hello", result);
    }
}
