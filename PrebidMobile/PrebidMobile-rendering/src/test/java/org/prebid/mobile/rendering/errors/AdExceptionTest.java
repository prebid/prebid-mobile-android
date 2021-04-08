package org.prebid.mobile.rendering.errors;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AdExceptionTest {

    private final String EXCEPTION_MESSAGE = "foo";

    @Test
    public void testErrorCodes() throws AdException {
        doExceptionTest(AdException.INTERNAL_ERROR);
        doExceptionTest(AdException.INIT_ERROR);
        doExceptionTest(AdException.SERVER_ERROR);
        doExceptionTest(AdException.INVALID_REQUEST);
    }

    private void doExceptionTest(String errorCode) {
        try {
            throwAdException(errorCode);
        }
        catch (AdException e) {
            assertEquals(errorCode + ": " + EXCEPTION_MESSAGE, e.getMessage());
            return;
        }

        fail("AdException never thrown");
    }

    private void throwAdException(String errorCode) throws AdException {
        throw new AdException(errorCode, EXCEPTION_MESSAGE);
    }
}
