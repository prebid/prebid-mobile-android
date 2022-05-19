package org.prebid.mobile.rendering.listeners;

import org.prebid.mobile.api.exceptions.InitError;

public interface SdkInitializationListener {

    void onSdkInit();

    void onSdkFailedToInit(InitError error);

}
