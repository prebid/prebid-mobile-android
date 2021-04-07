package com.openx.apollo.errors;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ServerWrongStatusCodeTest {

    @Test
    public void testServerWrongStatusCode() {
        ServerWrongStatusCode error = new ServerWrongStatusCode(404);
        assertEquals(AdException.SERVER_ERROR + ": Server returned 404 status code", error.getMessage());
    }
}
