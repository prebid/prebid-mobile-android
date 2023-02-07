package org.prebid.mobile.rendering.listeners;

import org.jetbrains.annotations.NotNull;
import org.prebid.mobile.api.data.InitializationStatus;
import org.prebid.mobile.api.exceptions.InitError;

public interface SdkInitializationListener {

    void onInitializationComplete(@NotNull InitializationStatus status);

    @Deprecated
    default void onSdkInit() {
    }

    @Deprecated
    default void onSdkFailedToInit(InitError error) {
    }

}
