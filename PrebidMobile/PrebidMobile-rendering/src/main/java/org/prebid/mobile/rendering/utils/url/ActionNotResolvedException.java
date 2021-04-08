package org.prebid.mobile.rendering.utils.url;

public class ActionNotResolvedException extends Exception {
    public ActionNotResolvedException(String message) {
        super(message);
    }

    public ActionNotResolvedException(Throwable cause) {
        super(cause);
    }
}
