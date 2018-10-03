package org.prebid.mobile.prebidserver.internal;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
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
    public static final String APPNEXUS_REQUEST_URL_NON_SECURE = "http://prebid.adnxs.com/pbs/v1/openrtb2/auction";
    public static final String APPNEXUS_REQUEST_URL_SECURE = "https://prebid.adnxs.com/pbs/v1/openrtb2/auction";
    public static final String RUBICON_REQUEST_URL_NON_SECURE = "http://prebid-server.rubiconproject.com/openrtb2/auction";
    public static final String RUBICON_REQUEST_URL_SECURE = "https://prebid-server.rubiconproject.com/openrtb2/auction";

    public static int connectionTimeOutMillis = 500;
    // request keys
    public static final String REQUEST_USER = "user";
    public static final String REQUEST_LANGUAGE = "language";
    public static final String REQUEST_DEVICE = "device";
    public static final String REQUEST_APP = "app";
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
    public static final String REQUEST_IFA = "ifa";
    public static final String REQUEST_OS = "os";
    public static final String REQUEST_OS_VERSION = "osv";
    public static final int REQUEST_KEY_LENGTH_MAX = 20;

    // Settings
    public static final String language = Locale.getDefault().getLanguage();
    public static final String deviceMake = Build.MANUFACTURER;
    public static final String deviceModel = Build.MODEL;
    public static final String os = "android";
    public static String userAgent = null;
    public static String sdk_version = "0.5";
    public static String pkgVersion = "";
    public static String appName = "";
    private static int mnc = -1;
    private static int mcc = -1;
    private static String carrierName = null;


    public static synchronized void update(final Context context) {
        if (userAgent == null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        WebView wv = new WebView(context);
                        userAgent = wv.getSettings().getUserAgentString();
                    } catch (AndroidRuntimeException e) {
                        userAgent = "unavailable";
                    }
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

}

