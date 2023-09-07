package org.prebid.mobile.rendering.sdk;

public class UserConsentFetcherTask {

    private UserConsentFetcherTask() {
    }

    public static void run() {
        ManagersResolver.getInstance().getUserConsentManager().initConsentValues();
    }

}
