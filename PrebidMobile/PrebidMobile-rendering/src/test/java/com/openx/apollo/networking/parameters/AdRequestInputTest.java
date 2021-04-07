package com.openx.apollo.networking.parameters;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

public class AdRequestInputTest {

    @Test
    public void testDeepCopy() throws Exception {
        AdRequestInput inputOriginal = new AdRequestInput();
        inputOriginal.getBidRequest().getUser().gender = "F";

        AdRequestInput inputCopy = inputOriginal.getDeepCopy();
        assertNotNull(inputCopy);

        // Test the objects are not the same
        assertNotSame(inputOriginal, inputCopy);
        assertNotSame(inputOriginal.getBidRequest(), inputCopy.getBidRequest());

        assertEquals(inputOriginal.getBidRequest().getJsonObject().toString(),
                     inputCopy.getBidRequest().getJsonObject().toString());
    }
}
