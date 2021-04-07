package com.openx.apollo.errors;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class VastParseErrorTest {

    @Test
    public void testVastParseError() {
        VastParseError vastErr = new VastParseError("VASTERROR");
        assertEquals(AdException.INTERNAL_ERROR + ": Failed to parse VAST. VASTERROR", vastErr.getMessage());
    }
}