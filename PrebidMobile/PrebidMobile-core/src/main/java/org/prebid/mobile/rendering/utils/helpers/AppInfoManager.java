/*
 *    Copyright 2018-2021 Prebid.org, Inc.
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

package org.prebid.mobile.rendering.utils.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.google.android.gms.security.ProviderInstaller;

import org.prebid.mobile.LogUtil;

public class AppInfoManager {
    private static final String TAG = AppInfoManager.class.getSimpleName();

    private static String sUserAgent;
    private static String sPackageName = null;
    private static String sAppName = null;
    private static String sAppVersion = null;

    public static void init(Context context) {
        initPackageInfo(context);
        patchSecurityProviderIfNeeded(context);
    }

    public static String getAppName() {
        return sAppName;
    }

    public static String getAppVersion() {
        return sAppVersion;
    }

    public static String getPackageName() {
        return sPackageName;
    }

    public static String getUserAgent() {
        return sUserAgent;
    }

    @VisibleForTesting
    public static void setAppName(String appName) {
        sAppName = appName;
    }

    @VisibleForTesting
    public static void setPackageName(String packageName) {
        sPackageName = packageName;
    }

    public static void setUserAgent(String userAgent) {
        sUserAgent = userAgent;
    }

    private static void initPackageInfo(Context context) {
        if (sPackageName == null || sAppName == null) {
            try {
                sPackageName = context.getPackageName();

                sAppName = "(unknown)";
                try {
                    PackageManager pm = context.getPackageManager();
                    ApplicationInfo appInfo = pm.getApplicationInfo(sPackageName, 0);
                    sAppName = (String) pm.getApplicationLabel(appInfo);
                    sAppVersion = pm.getPackageInfo(sPackageName, 0).versionName;
                }
                catch (Exception e) {
                    LogUtil.error(TAG, "Failed to get app name: " + Log.getStackTraceString(e));
                }
            }
            catch (Exception e) {
                LogUtil.error(TAG, "Failed to get package name: " + Log.getStackTraceString(e));
            }
        }
    }

    // IMPORTANT: This will only be executed for pre-lolipop versions due to security issue described here:
    // https://developer.android.com/training/articles/security-gms-provider
    // If this will be removed the publisher may face problems described here:
    // jira/browse/MOBILE-5295
    private static void patchSecurityProviderIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        try {
            ProviderInstaller.installIfNeededAsync(context, new ProviderInstaller.ProviderInstallListener() {
                @Override
                public void onProviderInstalled() {
                    LogUtil.debug(TAG, "Provider installed successfully");
                }

                @Override
                public void onProviderInstallFailed(int i, Intent intent) {
                    LogUtil.debug(TAG, "Provider installed failed. Error code: " + i);
                }
            });
        }
        catch (Throwable throwable) {
            LogUtil.error(TAG, "patchSecurityProvider Failed! Reason: " + Log.getStackTraceString(throwable));
        }
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        }
        else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}
