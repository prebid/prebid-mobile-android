package org.prebid.mobile;

public class PrebidMobile {
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
}
