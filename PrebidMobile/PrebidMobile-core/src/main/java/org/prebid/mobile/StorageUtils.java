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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

final class StorageUtils {

    private StorageUtils() {
    }

    //COPPA
    static final String PB_COPPAKey = "Prebid_COPPA";

    //GDPR
    static final String PBConsent_SubjectToGDPRKey = "Prebid_GDPR";
    static final String PBConsent_ConsentStringKey = "Prebid_GDPR_consent_strings";
    static final String PBConsent_PurposeConsents = "Prebid_GDPR_PurposeConsents";
    static final String IABConsent_SubjectToGDPRKey = "IABConsent_SubjectToGDPR";
    static final String IABConsent_ConsentStringKey = "IABConsent_ConsentString";

    //TCF 2.0 consent parameters
    static final String IABTCF_CONSENT_STRING = "IABTCF_TCString";
    static final String IABTCF_SUBJECT_TO_GDPR = "IABTCF_gdprApplies";

    static final String  IABTCF_PurposeConsents = "IABTCF_PurposeConsents";

    //CCPA
    static final String IABUSPrivacy_StringKey = "IABUSPrivacy_String";

    //COPPA
    static boolean getPbCoppa() throws PbContextNullException {

        SharedPreferences pref = getSharedPreferences();
        return pref.getBoolean(StorageUtils.PB_COPPAKey, false);
    }

    static void setPbCoppa(boolean value) throws PbContextNullException {

        SharedPreferences pref = getSharedPreferences();

        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(StorageUtils.PB_COPPAKey, value);
        editor.apply();

    }

    //GDPR Subject
    static boolean checkPbGdprSubject() throws PbContextNullException {
        return checkSharedPreferencesKey(StorageUtils.PBConsent_SubjectToGDPRKey);
    }

    static boolean getPbGdprSubject() throws PbContextNullException {
        SharedPreferences pref = getSharedPreferences();
        return pref.getBoolean(StorageUtils.PBConsent_SubjectToGDPRKey, false);

    }

    static void setPbGdprSubject(Boolean value) throws PbContextNullException {
        SharedPreferences pref = getSharedPreferences();
        SharedPreferences.Editor editor = pref.edit();
        if (value != null) {
            editor.putBoolean(StorageUtils.PBConsent_SubjectToGDPRKey, value);
        } else {
            editor.remove(StorageUtils.PBConsent_SubjectToGDPRKey);
        }
        editor.apply();
    }

    static boolean checkIabGdprSubject() throws PbContextNullException {
        return checkSharedPreferencesKey(StorageUtils.IABConsent_SubjectToGDPRKey);
    }

    static String getIabGdprSubject() throws PbContextNullException {

        SharedPreferences pref = getSharedPreferences();
        int iabTcfSubjectToGdpr = pref.getInt(IABTCF_SUBJECT_TO_GDPR, -1);
        String gdprSubject = iabTcfSubjectToGdpr == -1 ? "" : String.valueOf(iabTcfSubjectToGdpr);
        if(gdprSubject.isEmpty()){
            gdprSubject = pref.getString(StorageUtils.IABConsent_SubjectToGDPRKey, "");
        }
        return gdprSubject;
    }

    //GDPR Consent

    static boolean checkPbGdprConsent() throws PbContextNullException {
        return checkSharedPreferencesKey(StorageUtils.PBConsent_ConsentStringKey);
    }

    static String getPbGdprConsent() throws PbContextNullException {
        SharedPreferences pref = getSharedPreferences();
        return pref.getString(StorageUtils.PBConsent_ConsentStringKey, "");
    }

    static void setPbGdprConsent(String value) throws PbContextNullException {

        SharedPreferences pref = getSharedPreferences();
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(StorageUtils.PBConsent_ConsentStringKey, value);
        editor.apply();
    }

    static boolean checkIabGdprConsent() throws PbContextNullException {
        return checkSharedPreferencesKey(StorageUtils.IABConsent_ConsentStringKey);
    }

    static String getIabGdprConsent() throws PbContextNullException {
        SharedPreferences pref = getSharedPreferences();
        String gdprConsent = pref.getString(StorageUtils.IABTCF_CONSENT_STRING, "");
        if(gdprConsent.isEmpty()){
            gdprConsent = pref.getString(StorageUtils.IABConsent_ConsentStringKey, "");
        }
        return gdprConsent;
    }

    /**
     * Set the device access Consent by the publisher.
     *
     * @param deviceConsent set by the publisher to access the device data as per https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework
     */
    public static void setDeviceAccessConsent(String deviceConsent) throws PbContextNullException {
        if (!deviceConsent.isEmpty()) {
            SharedPreferences pref = getSharedPreferences();
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(StorageUtils.PBConsent_PurposeConsents, deviceConsent);
            editor.apply();
        }

    }

    /**
     * Get the device access Consent set by the publisher.
     *
     * @return A valid Base64 encode consent string as per https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework
     * or "" if not set
     */
    public static String checkDeviceAccessConsent() throws PbContextNullException {

        String deviceConsent = null;

        SharedPreferences pref = getSharedPreferences();
        if (pref.contains(PBConsent_PurposeConsents)) {
            deviceConsent = pref.getString(PBConsent_PurposeConsents, null);
        } else if (pref.contains(IABTCF_PurposeConsents)){
            deviceConsent = pref.getString(IABTCF_PurposeConsents, null);
        }

        return deviceConsent != null ? deviceConsent.substring(0, 1) : null;
    }

    //CCPA
    @Nullable
    static String getIabCcpa() throws PbContextNullException {
        SharedPreferences pref = getSharedPreferences();
        return pref.getString(StorageUtils.IABUSPrivacy_StringKey, null);
    }

    // private zone

    /**
     * @return SharedPreferences
     * @throws PbContextNullException
     */
    private static SharedPreferences getSharedPreferences() throws PbContextNullException {
        Context context = PrebidMobile.getApplicationContext();

        if (context == null) {
            throw new PbContextNullException("Context is null.");
        }

        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static boolean checkSharedPreferencesKey(String key) throws PbContextNullException {
        SharedPreferences pref = getSharedPreferences();
        return pref.contains(key);
    }
}
