package org.prebid.mobile.reflection.sdk;

import org.prebid.mobile.reflection.Reflection;
import org.prebid.mobile.rendering.sdk.deviceData.managers.UserConsentManager;

public class UserConsentManagerReflection {

    public static String getConstGdpr2Subject(UserConsentManager manager) {
        return Reflection.getFieldOf(manager, "GDPR_2_SUBJECT_KEY");
    }

    public static String getConstGdpr2Consent(UserConsentManager manager) {
        return Reflection.getFieldOf(manager, "GDPR_2_CONSENT_KEY");
    }

    public static String getConstGdpr2PurposeConsent(UserConsentManager manager) {
        return Reflection.getFieldOf(manager, "GDPR_2_PURPOSE_CONSENT_KEY");
    }

    public static String getConstUsPrivacyString(UserConsentManager manager) {
        return Reflection.getFieldOf(manager, "US_PRIVACY_KEY");
    }

    public static Boolean getPrebidCoppaConsent(UserConsentManager manager) {
        return Reflection.getFieldOf(manager, "prebidCoppaSubject");
    }

    public static Boolean getPrebidGdprSubject(UserConsentManager manager) {
        return Reflection.getFieldOf(manager, "prebidGdpr2Subject");
    }

    public static String getPrebidGdprConsent(UserConsentManager manager) {
        return Reflection.getFieldOf(manager, "prebidGdpr2Consent");
    }

    public static String getPrebidGdprPurposeConsent(UserConsentManager manager) {
        return Reflection.getFieldOf(manager, "prebidGdpr2PurposeConsents");
    }

    public static String getPrebidUsPrivacy(UserConsentManager manager) {
        return Reflection.getFieldOf(manager, "prebidUsPrivacyString");
    }

    public static void resetAllFields(UserConsentManager manager) {
        Reflection.setVariableTo(manager, "prebidGdpr2Subject", null);
        Reflection.setVariableTo(manager, "prebidGdpr2Consent", null);
        Reflection.setVariableTo(manager, "prebidGdpr2PurposeConsents", null);
        Reflection.setVariableTo(manager, "realGdpr2Consent", null);
        Reflection.setVariableTo(manager, "realGdpr2PurposeConsents", null);
        Reflection.setVariableTo(manager, "realGdpr2Subject", -1);
        Reflection.setVariableTo(manager, "prebidUsPrivacyString", null);
        Reflection.setVariableTo(manager, "realUsPrivacyString", null);
        Reflection.setVariableTo(manager, "prebidCoppaSubject", null);
    }

}
