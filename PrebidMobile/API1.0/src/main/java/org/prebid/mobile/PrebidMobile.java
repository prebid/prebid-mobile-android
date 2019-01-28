package org.prebid.mobile;

import android.content.Context;

import java.lang.ref.WeakReference;

public class PrebidMobile {

    static int timeoutMillis = 10000; // by default use 10000 milliseconds as timeout
    static boolean timeoutMillisUpdated = false;

    private PrebidMobile() {
    }

    private static String accountId = "";

    public static void setAccountId(String accountId) {
        PrebidMobile.accountId = accountId;
    }

    public static String getAccountId() {
        return accountId;
    }

    private static Host host = Host.APPNEXUS;

    public static void setHost(Host host) {
        PrebidMobile.host = host;
        timeoutMillisUpdated = false; // each time a developer sets a new Host for the SDK, we should re-calculate the time our millis
        timeoutMillis = 10000;
    }

    public static Host getHost() {
        return host;
    }

    private static boolean shareGeoLocation = false;

    public static void setShareGeoLocation(boolean share) {
        PrebidMobile.shareGeoLocation = share;
    }

    public static boolean isShareGeoLocation() {
        return shareGeoLocation;
    }

    private static WeakReference<Context> applicationContextWeak;

    public static void setApplicationContext(Context context) {
        applicationContextWeak = new WeakReference<Context>(context);
    }

    public static Context getApplicationContext() {
        if (applicationContextWeak != null) {
            return applicationContextWeak.get();
        }
        return null;
    }
}
