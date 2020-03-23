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

    @Nullable
    static Boolean getPbGdprSubject() throws PbContextNullException {

        if (!checkSharedPreferencesKey(StorageUtils.PBConsent_SubjectToGDPRKey)) {
            return null;
        }

        SharedPreferences pref = getSharedPreferences();
        return pref.getBoolean(StorageUtils.PBConsent_SubjectToGDPRKey, false);

    }

    static void setPbGdprSubject(@Nullable Boolean value) throws PbContextNullException {
        SharedPreferences pref = getSharedPreferences();
        SharedPreferences.Editor editor = pref.edit();
        if (value != null) {
            editor.putBoolean(StorageUtils.PBConsent_SubjectToGDPRKey, value);
        } else {
            editor.remove(StorageUtils.PBConsent_SubjectToGDPRKey);
        }
        editor.apply();
    }

    @Nullable
    static Boolean getIabGdprSubject() throws PbContextNullException {

        Boolean gdprSubject = null;

        SharedPreferences pref = getSharedPreferences();
        int gdprSubjectTcf2Default = -1;
        int gdprSubjectTcf2 = pref.getInt(StorageUtils.IABTCF_SUBJECT_TO_GDPR, gdprSubjectTcf2Default);

        if (gdprSubjectTcf2 != gdprSubjectTcf2Default) {
            if (gdprSubjectTcf2 == 1) {
                gdprSubject = true;
            } else if (gdprSubjectTcf2 == 0) {
                gdprSubject = false;
            }
        } else {
            String gdprSubjectTcf1 = pref.getString(StorageUtils.IABConsent_SubjectToGDPRKey, null);

            if (gdprSubjectTcf1 != null) {
                if ("1".equals(gdprSubjectTcf1)) {
                    return true;
                } else if ("0".equals(gdprSubjectTcf1)) {
                    return false;
                }
            }

        }

        return gdprSubject;
    }

    //GDPR Consent

    static String getPbGdprConsent() throws PbContextNullException {
        SharedPreferences pref = getSharedPreferences();
        return pref.getString(StorageUtils.PBConsent_ConsentStringKey, null);
    }

    static void setPbGdprConsent(@Nullable String value) throws PbContextNullException {

        SharedPreferences pref = getSharedPreferences();
        SharedPreferences.Editor editor = pref.edit();

        if (value != null) {
            editor.putString(StorageUtils.PBConsent_ConsentStringKey, value);
        } else {
            editor.remove(StorageUtils.PBConsent_ConsentStringKey);
        }
        editor.apply();
    }

    @Nullable
    static String getIabGdprConsent() throws PbContextNullException {
        SharedPreferences pref = getSharedPreferences();
        String gdprConsent = pref.getString(StorageUtils.IABTCF_CONSENT_STRING, null);
        if (gdprConsent == null) {
            gdprConsent = pref.getString(StorageUtils.IABConsent_ConsentStringKey, null);
        }
        return gdprConsent;
    }

    /**
     * Set the device access Consent by the publisher.
     *
     * @param purposeConsents set by the publisher to access the device data as per https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework
     */
    static void setPbPurposeConsents(@Nullable String purposeConsents) throws PbContextNullException {
        SharedPreferences pref = getSharedPreferences();
        SharedPreferences.Editor editor = pref.edit();

        String key = StorageUtils.PBConsent_PurposeConsents;

        if (purposeConsents != null) {
            editor.putString(key, purposeConsents);
        } else {
            editor.remove(key);
        }

        editor.apply();
    }

    @Nullable
    static String getPbPurposeConsents() {
        SharedPreferences pref = getSharedPreferences();

        return pref.getString(PBConsent_PurposeConsents, null);
    }

    @Nullable
    static String getIabPurposeConsents() {
        SharedPreferences pref = getSharedPreferences();

        return pref.getString(IABTCF_PurposeConsents, null);
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
