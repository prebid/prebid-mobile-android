package com.openx.apollo.interstitial;

public interface DialogEventListener {

    void onEvent(EventType eventType);

    enum EventType {
        CLOSED,
        SHOWN
    }
}
