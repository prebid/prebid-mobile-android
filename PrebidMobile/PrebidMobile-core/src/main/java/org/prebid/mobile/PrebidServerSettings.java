/*
 *    Copyright 2018-2019 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile;

import android.os.Build;

class PrebidServerSettings {
    static final String AN_UUID = "uuid2";
    static final String COOKIE_HEADER = "Cookie";
    static final String VERSION_ZERO_HEADER = "Set-cookie";
    static final String VERSION_ONE_HEADER = "Set-cookie2";
    static final String COOKIE_DOMAIN = "http://prebid.adnxs.com";
    // Prebid Server Constants
    // request keys
    static final String REQUEST_USER = "user";
    static final String REQUEST_LANGUAGE = "language";
    static final String REQUEST_DEVICE = "device";
    static final String REQUEST_APP = "app";
    static final String REQUEST_DEVICE_MAKE = "make";
    static final String REQUEST_DEVICE_MODEL = "model";
    static final String REQUEST_DEVICE_WIDTH = "w";
    static final String REQUEST_DEVICE_HEIGHT = "h";
    static final String REQUEST_DEVICE_PIXEL_RATIO = "pxratio";
    static final String REQUEST_MCC_MNC = "mccmnc";
    static final String REQUEST_LMT = "lmt";
    static final String REQUEST_CONNECTION_TYPE = "connectiontype";
    static final String REQUEST_CARRIER = "carrier";
    static final String REQUEST_USERAGENT = "ua";
    static final String REQUEST_GEO = "geo";
    static final String REQUEST_GEO_ACCURACY = "accuracy";
    static final String REQUEST_GEO_LON = "lon";
    static final String REQEUST_GEO_LAT = "lat";
    static final String REQUEST_GEO_AGE = "lastfix";
    static final String REQUEST_IFA = "ifa";
    static final String REQUEST_OS = "os";
    static final String REQUEST_OS_VERSION = "osv";
    static final int REQUEST_KEY_LENGTH_MAX = 20;

    // PrebidServerSettings
    static final String deviceMake = Build.MANUFACTURER;
    static final String deviceModel = Build.MODEL;
    static final String os = "android";
    static String sdk_version = "1.13.0-beta2";
    static String appName = "";
    private static int mnc = -1;
    private static int mcc = -1;
    private static String carrierName = null;


    static synchronized int getMCC() {
        return mcc;
    }

    static synchronized void setMCC(int mcc) {
        PrebidServerSettings.mcc = mcc;
    }

    static synchronized int getMNC() {
        return mnc;
    }

    static synchronized void setMNC(int mnc) {
        PrebidServerSettings.mnc = mnc;
    }

    static synchronized String getCarrierName() {
        return carrierName;
    }

    static synchronized void setCarrierName(String carrierName) {
        PrebidServerSettings.carrierName = carrierName;
    }

}

