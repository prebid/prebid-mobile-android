package org.prebid.mobile.rendering.interstitial;

public interface DialogEventListener {

    void onEvent(EventType eventType);

    enum EventType {
        CLOSED,
        SHOWN
    }
}
