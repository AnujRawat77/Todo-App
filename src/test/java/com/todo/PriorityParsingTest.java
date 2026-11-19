package com.todo;

import com.todo.model.Priority;
import com.todo.util.InputUtil;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PriorityParsingTest {

    @Test
    void parsesCaseInsensitively() {
        assertEquals(Priority.HIGH, Priority.fromString("high"));
        assertEquals(Priority.MEDIUM, Priority.fromString("  Medium "));
        assertEquals(Priority.LOW, Priority.fromString("LOW"));
    }

    @Test
    void parsesAliases() {
        assertEquals(Priority.HIGH, Priority.fromString("h"));
        assertEquals(Priority.MEDIUM, Priority.fromString("M"));
        assertEquals(Priority.LOW, Priority.fromString("l"));
    }

    @Test
    void blankOrNullReturnsNull() {
        assertNull(Priority.fromString(null));
        assertNull(Priority.fromString(""));
        assertNull(Priority.fromString("   "));
    }

    @Test
    void invalidThrows() {
        assertThrows(IllegalArgumentException.class, () -> Priority.fromString("urgent"));
    }

    private static Scanner scannerOf(String text) {
        return new Scanner(new ByteArrayInputStream(text.getBytes()));
    }

    @Test
    void readPriorityDefaultsToMediumOnEmptyInput() {
        Priority p = InputUtil.readPriorityOrDefault(scannerOf("\n"), "p: ", Priority.MEDIUM);
        assertEquals(Priority.MEDIUM, p);
    }

    @Test
    void readPriorityRepromptsUntilValid() {
        Priority p = InputUtil.readPriorityOrDefault(scannerOf("bogus\n42\nlow\n"), "p: ", Priority.MEDIUM);
        assertEquals(Priority.LOW, p);
    }

    @Test
    void readIntRepromptsUntilNumeric() {
        assertEquals(7, InputUtil.readInt(scannerOf("abc\n\n7\n"), "id: "));
    }
}
