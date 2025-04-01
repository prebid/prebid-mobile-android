package org.prebid.mobile.rendering.listeners;

import org.jetbrains.annotations.NotNull;
import org.prebid.mobile.api.data.InitializationStatus;

public interface SdkInitializationListener {

    void onInitializationComplete(@NotNull InitializationStatus status);

}
