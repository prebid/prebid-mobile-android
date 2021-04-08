package org.prebid.mobile.rendering.networking.exception;

/**
 * Base exception holder class
 */
public class BaseExceptionHolder extends BaseExceptionProvider {

    private Exception mException;

    public BaseExceptionHolder() {
        // Create without exception
    }

    public BaseExceptionHolder(Exception exception) {
        mException = exception;
    }

    public void setException(Exception exception) {
        mException = exception;
    }

    @Override
    public Exception getException() {
        return mException;
    }
}
