package org.prebid.mobile.prebidserver.internal;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.webkit.WebView;

import java.util.Locale;

public class Settings {
    public static final String TAG = "PrebidServer";
    public static final String AN_UUID = "uuid2";
    public static final String COOKIE_HEADER = "Cookie";
    public static final String VERSION_ZERO_HEADER = "Set-cookie";
    public static final String VERSION_ONE_HEADER = "Set-cookie2";
    public static final String COOKIE_DOMAIN = "http://prebid.adnxs.com";
    // Prebid Server Constants
    // connection settings
    public static final String REQUEST_URL_NON_SECURE = "http://prebid.adnxs.com/pbs/v1/auction";
    public static final String REQUEST_URL_SECURE = "https://prebid.adnxs.com/pbs/v1/auction";
    public static int connectionTimeOutMillis = 500;
    // request keys
    public static final String REQUEST_CACHE_MARKUP = "cache_markup";
    public static final String REQUEST_SORT_BIDS = "sort_bids";
    public static final String REQUEST_TID = "tid";
    public static final String REQUEST_ACCOUNT_ID = "account_id";
    public static final String REQUEST_MAX_KEY = "max_key_length";
    public static final String REQUEST_AD_UNITS = "ad_units";
    public static final String REQUEST_CONFIG_ID = "config_id";
    public static final String REQUEST_CODE = "code";
    public static final String REQUEST_WIDTH = "w";
    public static final String REQUEST_HEIGHT = "h";
    public static final String REQUEST_SIZES = "sizes";
    public static final String REQUEST_USER = "user";
    public static final String REQUEST_AGE = "age";
    public static final String REQUEST_GENDER = "gender";
    public static final String REQUEST_LANGUAGE = "language";
    public static final String REQUEST_DEVICE = "device";
    public static final String REQUEST_APP = "app";
    public static final String REQUEST_APP_NAME = "name";
    public static final String REQUEST_APP_BUNDLE = "bundle";
    public static final String REQUEST_APP_DOMAIN = "domain";
    public static final String REQUEST_APP_STOREURL = "storeurl";
    public static final String REQUEST_APP_PRIVACY = "privacypolicy";
    public static final String REQUEST_APP_VERSION = "ver";
    public static final String REQUEST_KEYWORDS = "keywords";
    public static final String REQUEST_DEVICE_MAKE = "make";
    public static final String REQUEST_DEVICE_MODEL = "model";
    public static final String REQUEST_DEVICE_WIDTH = "w";
    public static final String REQUEST_DEVICE_HEIGHT = "h";
    public static final String REQUEST_DEVICE_PIXEL_RATIO = "pxratio";
    public static final String REQUEST_MCC_MNC = "mccmnc";
    public static final String REQUEST_LMT = "lmt";
    public static final String REQUEST_CONNECTION_TYPE = "connectiontype";
    public static final String REQUEST_CARRIER = "carrier";
    public static final String REQUEST_USERAGENT = "ua";
    public static final String REQUEST_GEO = "geo";
    public static final String REQUEST_GEO_ACCURACY = "accuracy";
    public static final String REQUEST_GEO_LON = "lon";
    public static final String REQEUST_GEO_LAT = "lat";
    public static final String REQUEST_GEO_AGE = "lastfix";
    public static final String REQUEST_DEVTIME = "devtime";
    public static final String REQUEST_IFA = "ifa";
    public static final String REQUEST_OS = "os";
    public static final String REQUEST_OS_VERSION = "osv";
    public static final String REQUEST_KEY = "key";
    public static final String REQUEST_VALUE = "value";
    public static final String REQUEST_SDK = "sdk";
    public static final String REQUEST_SDK_SOURCE = "source";
    public static final String REQUEST_SDK_VERSION = "version";
    public static final String REQUEST_SDK_PLATFORM = "platform";
    public static final String REQUEST_SDK_MOBILE = "prebid-mobile";
    public static final String REQUEST_SDK_ANDROID = "android";
    public static final int REQUEST_KEY_LENGTH_MAX = 20;

    // response
    public static final String RESPONSE_TID = "tid";
    public static final String RESPONSE_BIDS = "bids";
    public static final String RESPONSE_CODE = "code";
    public static final String RESPONSE_PRICE = "price";
    public static final String RESPONSE_BIDDER = "bidder";
    public static final String RESPONSE_STATUS = "status";
    public static final String RESPONSE_STATUS_OK = "OK";
    public static final String RESPONSE_TARGETING = "ad_server_targeting";
    public static final String RESPONSE_CREATIVE = "hb_creative_loadtype";
    public static final String RESPONSE_CACHE_ID = "hb_cache_id";

    // Settings
    public static final String language = Locale.getDefault().getLanguage();
    public static final String deviceMake = Build.MANUFACTURER;
    public static final String deviceModel = Build.MODEL;
    public static final String os = "android";
    public static String userAgent = null;
    public static String sdk_version = "0.1.0";
    public static String pkgVersion = "";
    public static String appName = "";
    static int mnc = -1;
    static int mcc = -1;
    static String carrierName = null;
    static String app_id = null;
    static String domain = "";
    static String storeUrl = "";
    static int privacyPolicy = 0;

    public static synchronized void update(final Context context) {
        if (userAgent == null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {

                    WebView wv = new WebView(context);
                    userAgent = wv.getSettings().getUserAgentString();

                }
            });
        }
        if (TextUtils.isEmpty(pkgVersion)) {
            try {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                pkgVersion = pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (TextUtils.isEmpty(appName)) {
            ApplicationInfo applicationInfo = context.getApplicationInfo();
            int stringId = applicationInfo.labelRes;
            if (stringId == 0) {
                if (applicationInfo.nonLocalizedLabel != null) {
                    appName = applicationInfo.nonLocalizedLabel.toString();
                }
            } else {
                appName = context.getString(stringId);
            }
        }
    }

    public static synchronized int getConnectionTimeOutMillis() {
        return connectionTimeOutMillis;
    }

    public static synchronized void setConnectionTimeOutMillis(int timeOutMillis) {
        Settings.connectionTimeOutMillis = timeOutMillis;
    }

    public static synchronized int getMCC() {
        return mcc;
    }

    public static synchronized void setMCC(int mcc) {
        Settings.mcc = mcc;
    }

    public static synchronized int getMNC() {
        return mnc;
    }

    public static synchronized void setMNC(int mnc) {
        Settings.mnc = mnc;
    }

    public static synchronized String getCarrierName() {
        return carrierName;
    }

    public static synchronized void setCarrierName(String carrierName) {
        Settings.carrierName = carrierName;
    }

    public static synchronized String getAppID() {
        return app_id;
    }

    public static synchronized void setAppID(String app_id) {
        Settings.app_id = app_id;
    }

    public static synchronized void setDomain(String domain) {
        Settings.domain = domain;
    }

    public static synchronized String getStoreUrl() {
        return storeUrl;
    }

    public static synchronized void setPrivacyPolicy(int privacyPolicy) {
        Settings.privacyPolicy = privacyPolicy;
    }

    public static synchronized int getPrivacyPolicy() {
        return privacyPolicy;
    }

    public static synchronized void setStoreUrl(String storeUrl) {
        Settings.storeUrl = storeUrl;
    }

    public static synchronized String getDomain() {
        return domain;
    }
}

