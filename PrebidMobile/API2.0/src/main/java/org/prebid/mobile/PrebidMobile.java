package org.prebid.mobile;

public class PrebidMobile {
    private PrebidMobile() {
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

    private static boolean shouldUseSecureConnection = false;

    public static void setShouldUseSecureConnection(boolean should) {
        PrebidMobile.shouldUseSecureConnection = should;
    }

    public static boolean shouldUseSecureConnection() {
        return shouldUseSecureConnection;
    }

    private static int timeout = 500; // by default wait 500 milliseconds for response to come back

    public static void setTimeout(int timeout) {
        PrebidMobile.timeout = timeout;
    }

    public static int getTimeout() {
        return PrebidMobile.timeout;
    }
}
