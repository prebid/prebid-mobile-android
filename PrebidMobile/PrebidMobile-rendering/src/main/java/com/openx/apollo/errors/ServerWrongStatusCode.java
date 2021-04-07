package com.openx.apollo.errors;

/**
 * Error will be thrown when server has responded with error status code.
 */
public class ServerWrongStatusCode extends AdException {
    public ServerWrongStatusCode(int code) {
        super(SERVER_ERROR, "Server returned " + code + " status code");
    }
}
