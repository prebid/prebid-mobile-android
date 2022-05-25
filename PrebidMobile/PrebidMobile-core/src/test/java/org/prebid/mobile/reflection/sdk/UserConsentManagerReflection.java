package org.prebid.mobile.reflection.sdk;

import org.prebid.mobile.reflection.Reflection;
import org.prebid.mobile.rendering.sdk.deviceData.managers.UserConsentManager;

public class UserConsentManagerReflection {

    public static String getConstGdpr1Subject(UserConsentManager manager) {
        return Reflection.getFieldOf(manager, "GDPR_1_SUBJECT");
    }

    public static String getConstGdpr1Consent(UserConsentManager manager) {
        return Reflection.getFieldOf(manager, "GDPR_1_CONSENT");
    }

    public static String getConstGdpr2CmpSdkId(UserConsentManager manager) {
        return Reflection.getFieldOf(manager, "GDPR_2_CMP_SDK_ID");
    }

    public static String getConstGdpr2Subject(UserConsentManager manager) {
        return Reflection.getFieldOf(manager, "GDPR_2_SUBJECT");
    }

    public static String getConstGdpr2Consent(UserConsentManager manager) {
        return Reflection.getFieldOf(manager, "GDPR_2_CONSENT");
    }

    public static String getConstGdpr2PurposeConsent(UserConsentManager manager) {
        return Reflection.getFieldOf(manager, "GDPR_2_PURPOSE_CONSENT");
    }

    public static String getConstUsPrivacyString(UserConsentManager manager) {
        return Reflection.getFieldOf(manager, "US_PRIVACY_STRING");
    }

    public static String getConstCoppaCustomKey(UserConsentManager manager) {
        return Reflection.getFieldOf(manager, "COPPA_SUBJECT_CUSTOM_KEY");
    }

    public static String getConstGdprPrebidSubject(UserConsentManager manager) {
        return Reflection.getFieldOf(manager, "GDPR_PREBID_SUBJECT");
    }

    public static String getConstGdprPrebidConsent(UserConsentManager manager) {
        return Reflection.getFieldOf(manager, "GDPR_PREBID_CONSENT");
    }

    public static String getConstGdprPrebidPurposeConsent(UserConsentManager manager) {
        return Reflection.getFieldOf(manager, "GDPR_PREBID_PURPOSE_CONSENT");
    }

}
