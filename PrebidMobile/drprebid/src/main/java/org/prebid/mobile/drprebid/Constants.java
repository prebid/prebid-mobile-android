package org.prebid.mobile.drprebid;

public final class Constants {
    public static final class Preferences {
        public static final String WELCOME_SHOWN = "has_welcome_been_shown";
        public static final String QR_CODE_SCAN_CACHE = "scanned_qr_code_cache";
    }

    public static final class Settings {
        public static final String AD_FORMAT = "ad_format";
        public static final String AD_SIZE = "ad_size";
        public static final String AD_SERVER = "ad_server";
        public static final String BID_PRICE = "bid_price";
        public static final String AD_UNIT_ID = "ad_unit_id";
        public static final String PREBID_SERVER = "prebid_server";
        public static final String PREBID_SERVER_CUSTOM_URL = "prebid_server_custom_url";
        public static final String ACCOUNT_ID = "account_id";
        public static final String CONFIG_ID = "config_id";

        public static final class AdFormatCodes {
            public static final int BANNER = 1;
            public static final int INTERSTITIAL = 2;
        }

        public static final class AdSizeCodes {
            public static final int SIZE_300x250 = 1;
            public static final int SIZE_300x600 = 2;
            public static final int SIZE_320x50 = 3;
            public static final int SIZE_320x100 = 4;
            public static final int SIZE_320x480 = 5;
            public static final int SIZE_728x90 = 6;
        }

        public static final class AdServerCodes {
            public static final int GOOGLE_AD_MANAGER = 1;
            public static final int MOPUB = 2;
        }

        public static final class PrebidServerCodes {
            public static final int RUBICON = 1;
            public static final int APPNEXUS = 2;
            public static final int CUSTOM = 3;
        }
    }
}
