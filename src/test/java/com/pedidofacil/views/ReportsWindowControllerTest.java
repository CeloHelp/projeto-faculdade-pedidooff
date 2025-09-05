package com.pedidofacil.views;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReportsWindowControllerTest {

    @Test
    void testFormatDateLabel() {
        ReportsWindowController controller = new ReportsWindowController(null); // ViewModel is not needed for this test
        String formattedDate = controller.formatDateLabel("2023-01-15");
        assertEquals("15/01", formattedDate);
    }
}