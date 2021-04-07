package com.openx.apollo.utils.url;

public class ActionNotResolvedException extends Exception {
    public ActionNotResolvedException(String message) {
        super(message);
    }

    public ActionNotResolvedException(Throwable cause) {
        super(cause);
    }
}
