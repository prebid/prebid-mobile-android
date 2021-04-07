package com.openx.apollo.utils.helpers;

import android.text.TextUtils;

import com.openx.apollo.sdk.ManagersResolver;

public class MraidUtils {

    public static boolean isFeatureSupported(String feature) {
        if (TextUtils.isEmpty(feature)) {
            return false;
        }

        switch (feature) {
            case "sms":
            case "tel":
                return ManagersResolver.getInstance().getDeviceManager().hasTelephony();
            case "calendar":
                return true;
            case "storePicture":
                return ManagersResolver.getInstance().getDeviceManager().canStorePicture();
            case "inlineVideo":
                return true;
            case "location":
                return ManagersResolver.getInstance().getDeviceManager().hasGps();
            case "vpaid":
                return false;
            default:
                return false;
        }
    }
}
