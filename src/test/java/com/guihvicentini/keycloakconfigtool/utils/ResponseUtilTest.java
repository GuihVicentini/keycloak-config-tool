package com.guihvicentini.keycloakconfigtool.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResponseUtilTest {

    @Test
    public void whenClassInstance_throwException() {
        Exception exception = assertThrows(IllegalStateException.class,
                ResponseUtil::new);

        assertTrue(exception.getMessage().contains("Utility class"));
    }
}
