package org.prebid.mobile.rendering.sdk;

import android.content.Context;

import androidx.annotation.Nullable;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.listeners.SdkInitializationListener;
import org.prebid.mobile.rendering.sdk.deviceData.managers.UserConsentManager;

/**
 * Helps to get/set consents values, checks if UserConsentManager was created
 * (it's created in {@link org.prebid.mobile.PrebidMobile#initializeSdk(Context, SdkInitializationListener)}).
 * If the consent manager created it gets/sets value, otherwise logs error and returns null.
 */
public class UserConsentUtils {

    private static final String TAG = UserConsentUtils.class.getSimpleName();

    /* -------------------- COPPA -------------------- */

    @Nullable
    public static Boolean tryToGetSubjectToCoppa() {
        return getIfManagerExists("getSubjectToCoppa", UserConsentManager::getSubjectToCoppa);
    }

    public static void tryToSetSubjectToCoppa(@Nullable Boolean isCoppa) {
        doIfManagerExists("setSubjectToCoppa", manager -> manager.setSubjectToCoppa(isCoppa));
    }

    /* -------------------- GDPR -------------------- */

    @Nullable
    public static Boolean tryToGetSubjectToGdpr() {
        return getIfManagerExists("getSubjectToGdpr", UserConsentManager::getSubjectToGdpr);
    }

    public static void tryToSetPrebidSubjectToGdpr(@Nullable Boolean value) {
        doIfManagerExists("setPrebidSubjectToGdpr", manager -> manager.setSubjectToGdpr(value));
    }

    @Nullable
    public static String tryToGetGdprConsent() {
        return getIfManagerExists("getGdprConsent", UserConsentManager::getGdprConsent);
    }

    public static void tryToSetPrebidGdprConsent(@Nullable String consent) {
        doIfManagerExists("setGdprConsent", manager -> manager.setGdprConsent(consent));
    }

    @Nullable
    public static String tryToGetGdprPurposeConsents() {
        return getIfManagerExists("getPurposeConsents", UserConsentManager::getGdprPurposeConsents);
    }

    public static void tryToSetPrebidGdprPurposeConsents(@Nullable String consent) {
        doIfManagerExists("setPrebidPurposeConsents", manager -> manager.setGdprPurposeConsents(consent));
    }

    @Nullable
    public static Boolean tryToGetGdprPurposeConsent(int index) {
        return getIfManagerExists("getGdprPurposeConsent", manager -> manager.getGdprPurposeConsent(index));
    }

    public static Boolean tryToGetDeviceAccessConsent() {
        return getIfManagerExists("setPurposeConsents", UserConsentManager::canAccessDeviceData);
    }


    /* -------------------- Private region -------------------- */

    private static void doIfManagerExists(
        String method,
        SuccessfulSetter setter
    ) {
        UserConsentManager manager = ManagersResolver.getInstance().getUserConsentManager();
        if (manager != null) {
            setter.set(manager);
        } else {
            LogUtil.error(TAG, "You can't call " + method + "() before PrebidMobile.initializeSdk().");
        }
    }

    private static <T> T getIfManagerExists(
        String method,
        SuccessfulGetter<T> getter
    ) {
        UserConsentManager manager = ManagersResolver.getInstance().getUserConsentManager();
        if (manager != null) {
            return getter.get(manager);
        } else {
            LogUtil.error(TAG, "You can't call " + method + "() before PrebidMobile.initializeSdk().");
        }
        return null;
    }

    private interface SuccessfulSetter {

        void set(UserConsentManager manager);

    }

    private interface SuccessfulGetter<T> {

        T get(UserConsentManager manager);

    }

}
