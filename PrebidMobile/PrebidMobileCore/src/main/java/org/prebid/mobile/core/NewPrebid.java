package org.prebid.mobile.core;

public class NewPrebid {
    private NewPrebid() {
    }

    public static final String TAG = "NewPrebidAPI";

    private static String accountId = "";

    public static void setAccountId(String accountId) {
        NewPrebid.accountId = accountId;
    }

    public static String getAccountId() {
        return accountId;
    }

    private static Prebid.Host host = Prebid.Host.APPNEXUS;

    public static void setHost(Prebid.Host host) {
        NewPrebid.host = host;
    }

    public static Prebid.Host getHost() {
        return host;
    }

    private static boolean shouldUseSecureConnection = false;

    public static void setShouldUseSecureConnection(boolean should) {
        NewPrebid.shouldUseSecureConnection = should;
    }

    public static boolean shouldUseSecureConnection() {
        return shouldUseSecureConnection;
    }

    private static int timeOut = 500; // by default wait 500 milliseconds for response to come back

    public static void setTimeOUt(int timeOut) {
        NewPrebid.timeOut = timeOut;
    }

    public static int getTimeOut() {
        return NewPrebid.timeOut;
    }
}
