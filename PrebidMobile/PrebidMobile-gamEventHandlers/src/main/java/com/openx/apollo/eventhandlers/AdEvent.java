package com.openx.apollo.eventhandlers;

public enum AdEvent {
    APP_EVENT_RECEIVED,
    LOADED,
    CLOSED,
    CLICKED,
    DISPLAYED,
    REWARD_EARNED,
    FAILED;

    private int errorCode = -1;

    void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    int getErrorCode() {
        return errorCode;
    }
}
