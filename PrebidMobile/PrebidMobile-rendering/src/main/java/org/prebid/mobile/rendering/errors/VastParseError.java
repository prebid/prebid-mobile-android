package org.prebid.mobile.rendering.errors;

/**
 * Error will be thrown when unknown problem has appeared.
 */
public class VastParseError extends AdException {
    public VastParseError(String err) {
        super(INTERNAL_ERROR, "Failed to parse VAST. " + err);
    }
}
