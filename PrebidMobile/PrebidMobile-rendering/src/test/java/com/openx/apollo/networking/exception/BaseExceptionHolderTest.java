package com.openx.apollo.networking.exception;

import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

public class BaseExceptionHolderTest {

    @Test
    public void testBaseExceptionHolderWithException() {
        Exception error = new Exception();
        BaseExceptionHolder holder = new BaseExceptionHolder(error);
        assertNotNull(holder.getException());
        BaseExceptionHolder nullHolder = new BaseExceptionHolder(null);
        assertNull(nullHolder.getException());
    }

}