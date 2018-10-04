package org.prebid.mobile;

public class Prebid {
    private Prebid() {
    }

    public enum Host {
        APPNEXUS("http://prebid.adnxs.com/pbs/v1/openrtb2/auction", "https://prebid.adnxs.com/pbs/v1/openrtb2/auction"),
        RUBICON("http://prebid-server.rubiconproject.com/openrtb2/auction", "https://prebid-server.rubiconproject.com/openrtb2/auction"),
        CUSTOM("", "");

        private String nonSecureUrl;
        private String secureUrl;

        Host(String nonSecureUrl, String secureUrl) {
            this.nonSecureUrl = nonSecureUrl;
            this.secureUrl = secureUrl;
        }

        public String getNonSecureUrl() {
            return this.nonSecureUrl;
        }

        public String getSecureUrl() {
            return this.secureUrl;
        }

        public void setNonSecureUrl(String url) {
            if (this.equals(CUSTOM)) {
                this.nonSecureUrl = url;
            }
        }

        public void setSecureUrl(String url) {
            if (this.equals(CUSTOM)) {
                this.secureUrl = url;
            }
        }
    }

    public static final String TAG = "NewPrebidAPI";

    private static String accountId = "";

    public static void setAccountId(String accountId) {
        Prebid.accountId = accountId;
    }

    public static String getAccountId() {
        return accountId;
    }

    private static Host host = Host.APPNEXUS;

    public static void setHost(Host host) {
        Prebid.host = host;
    }

    public static Host getHost() {
        return host;
    }

    private static boolean shouldUseSecureConnection = false;

    public static void setShouldUseSecureConnection(boolean should) {
        Prebid.shouldUseSecureConnection = should;
    }

    public static boolean shouldUseSecureConnection() {
        return shouldUseSecureConnection;
    }

    private static int timeOut = 500; // by default wait 500 milliseconds for response to come back

    public static void setTimeOUt(int timeOut) {
        Prebid.timeOut = timeOut;
    }

    public static int getTimeOut() {
        return Prebid.timeOut;
    }
}
