package org.prebid.mobile.rendering.errors;

/**
 * Base error. Maintaining error description.
 */
public class AdException extends Exception {
    public static final String INVALID_REQUEST = "Invalid request";

    public static final String INTERNAL_ERROR = "SDK internal error";
    public static final String INIT_ERROR = "Initialization failed";
    public static final String SERVER_ERROR = "Server error";
    public static final String THIRD_PARTY = "Third Party SDK";

    private String mMessage;

    public void setMessage(String msg) {
        mMessage = msg;
    }

    /**
     * Error description.
     *
     * @return description
     */
    @Override
    public String getMessage() {
        return mMessage;
    }

    public AdException(String type, String message) {
        setMessage(type + ": " + message);
    }
}
