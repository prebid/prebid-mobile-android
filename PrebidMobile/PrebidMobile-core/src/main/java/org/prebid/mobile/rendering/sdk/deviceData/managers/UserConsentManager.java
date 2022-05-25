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

package org.prebid.mobile.rendering.sdk.deviceData.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.sdk.BaseManager;

public class UserConsentManager extends BaseManager {

    private static final String TAG = UserConsentManager.class.getSimpleName();

    static final int NOT_ASSIGNED = -1;

    // TCF v1 constants
    private static final String GDPR_1_SUBJECT = "IABConsent_SubjectToGDPR";
    private static final String GDPR_1_CONSENT = "IABConsent_ConsentString";

    // TCF v2 constants
    private static final String GDPR_2_CMP_SDK_ID = "IABTCF_CmpSdkID";
    private static final String GDPR_2_SUBJECT = "IABTCF_gdprApplies";
    private static final String GDPR_2_CONSENT = "IABTCF_TCString";
    private static final String GDPR_2_PURPOSE_CONSENT = "IABTCF_PurposeConsents";

    // CCPA
    private static final String US_PRIVACY_STRING = "IABUSPrivacy_String";

    // COPPA
    private static final String COPPA_SUBJECT_CUSTOM_KEY = "Prebid_COPPA";

    private Boolean isSubjectToCoppa;

    private String usPrivacyString;

    private String gdprSubject;
    private String gdprConsent;

    private String gdpr2Consent;
    private int gdpr2Subject = NOT_ASSIGNED;
    private String gdpr2PurposeConsent;

    /**
     * The unsigned integer ID of CMP SDK. Less than 0 values should be considered invalid.
     */
    private int gdpr2CmpSdkId = NOT_ASSIGNED;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;

    @Override
    public void init(Context context) {
        super.init(context);

        if (super.isInit() && context != null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            initConsentValuesAtStart(sharedPreferences);

            onSharedPreferenceChangeListener = this::updateConsentValue;

            sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        }
    }

    private void initConsentValuesAtStart(SharedPreferences preferences) {
        updateConsentValue(preferences, GDPR_1_SUBJECT);
        updateConsentValue(preferences, GDPR_1_CONSENT);
        updateConsentValue(preferences, GDPR_2_CMP_SDK_ID);
        updateConsentValue(preferences, GDPR_2_SUBJECT);
        updateConsentValue(preferences, GDPR_2_CONSENT);
        updateConsentValue(preferences, US_PRIVACY_STRING);
        updateConsentValue(preferences, GDPR_2_PURPOSE_CONSENT);
        updateConsentValue(preferences, COPPA_SUBJECT_CUSTOM_KEY);
    }

    /**
     * Automatically updates consents values in the manager when they are changed.
     */
    private void updateConsentValue(
        SharedPreferences preferences,
        @Nullable String key
    ) {
        if (key == null) return;

        switch (key) {
            case GDPR_1_SUBJECT:
                gdprSubject = preferences.getString(GDPR_1_SUBJECT, null);
            case GDPR_1_CONSENT:
                gdprConsent = preferences.getString(GDPR_1_CONSENT, null);
            case GDPR_2_CMP_SDK_ID:
                gdpr2CmpSdkId = preferences.getInt(GDPR_2_CMP_SDK_ID, NOT_ASSIGNED);
            case GDPR_2_SUBJECT:
                gdpr2Subject = preferences.getInt(GDPR_2_SUBJECT, NOT_ASSIGNED);
            case GDPR_2_CONSENT:
                gdpr2Consent = preferences.getString(GDPR_2_CONSENT, null);
            case US_PRIVACY_STRING:
                usPrivacyString = preferences.getString(US_PRIVACY_STRING, null);
            case GDPR_2_PURPOSE_CONSENT:
                gdpr2PurposeConsent = preferences.getString(GDPR_2_PURPOSE_CONSENT, null);
            case COPPA_SUBJECT_CUSTOM_KEY:
                if (sharedPreferences.contains(COPPA_SUBJECT_CUSTOM_KEY)) {
                    isSubjectToCoppa = sharedPreferences.getBoolean(COPPA_SUBJECT_CUSTOM_KEY, false);
                } else {
                    isSubjectToCoppa = null;
                }
        }
    }

    @Nullable
    public Boolean getSubjectToCoppa() {
        return isSubjectToCoppa;
    }

    public void setSubjectToCoppa(@Nullable Boolean value) {
        if (value != null) {
            sharedPreferences
                .edit()
                .putBoolean(COPPA_SUBJECT_CUSTOM_KEY, value)
                .apply();
        } else {
            sharedPreferences
                .edit()
                .remove(COPPA_SUBJECT_CUSTOM_KEY)
                .apply();
        }
    }

    @Nullable
    public Integer getCmpSdkIdForGdprTcf2() {
        return gdpr2CmpSdkId;
    }

    public void setCmpSdkIdForGdprTcf2(@Nullable Integer id) {
        sharedPreferences
            .edit()
            .putInt(GDPR_2_CMP_SDK_ID, id != null ? id : NOT_ASSIGNED)
            .apply();
    }

    @Nullable
    public String getSubjectToGdpr() {
        if (shouldUseTcfV2()) {
            return getSubjectToGdprTcf2();
        }

        return gdprSubject;
    }

    public Boolean getSubjectToGdprBoolean() {
        String subject = getSubjectToGdpr();
        if (subject != null) {
            if (subject.equals("0")) {
                return false;
            } else if (subject.equals("1")) {
                return true;
            }
        }
        return null;
    }

    public void setSubjectToGdpr(@Nullable Boolean value) {
        if (value == null) {
            sharedPreferences
                .edit()
                .remove(GDPR_1_SUBJECT)
                .remove(GDPR_2_SUBJECT)
                .apply();
        } else if (!shouldUseTcfV2()) {
            sharedPreferences
                .edit()
                .putString(GDPR_1_SUBJECT, value ? "1" : "0")
                .apply();
        } else {
            sharedPreferences
                .edit()
                .putInt(GDPR_2_SUBJECT, value ? 1 : 0)
                .apply();
        }
    }

    @Nullable
    public String getGdprConsent() {
        if (shouldUseTcfV2()) {
            return gdpr2Consent;
        }

        return gdprConsent;
    }

    public void setGdprConsent(@Nullable String consent) {
        if (consent == null) {
            sharedPreferences
                .edit()
                .remove(GDPR_1_CONSENT)
                .remove(GDPR_2_CONSENT)
                .apply();
        } else if (!shouldUseTcfV2()) {
            sharedPreferences
                .edit()
                .putString(GDPR_1_CONSENT, consent)
                .apply();
        } else {
            sharedPreferences
                .edit()
                .putString(GDPR_2_CONSENT, consent)
                .apply();
        }
    }

    @Nullable
    public String getGdprPurposeConsents() {
        return gdpr2PurposeConsent;
    }

    @Nullable
    public Boolean getGdprPurposeConsent(int index) {
        String consents = gdpr2PurposeConsent;

        if (consents != null && consents.length() > index) {
            char consentChar = consents.charAt(index);
            if (consentChar == '1') {
                return true;
            } else if (consentChar == '0') {
                return false;
            } else {
                LogUtil.warning("Can't get GDPR purpose consent, unsupported char: " + consentChar);
            }
        }

        return null;
    }

    public void setGdprPurposeConsents(@Nullable String consent) {
        sharedPreferences
            .edit()
            .putString(GDPR_2_PURPOSE_CONSENT, consent)
            .apply();
    }


    public String getUsPrivacyString() {
        return usPrivacyString;
    }

    public void setUsPrivacyString(@Nullable String value) {
        sharedPreferences
            .edit()
            .putString(US_PRIVACY_STRING, value)
            .apply();
    }

    /**
     * Truth table. Fetches advertising identifier based TCF 2.0 Purpose1 value.
     * deviceAccessConsent=true   deviceAccessConsent=false  deviceAccessConsent undefined
     * gdprApplies=false        Yes, read IDFA             No, don’t read IDFA           Yes, read IDFA
     * gdprApplies=true         Yes, read IDFA             No, don’t read IDFA           No, don’t read IDFA
     * gdprApplies=undefined    Yes, read IDFA             No, don’t read IDFA           Yes, read IDFA
     */
    public boolean canAccessDeviceData() {
        final String subjectToGdpr = getSubjectToGdpr();
        final int deviceConsentIndex = 0;

        Boolean gdprApplies = TextUtils.isEmpty(subjectToGdpr) ? null : "1".equals(subjectToGdpr);
        Boolean deviceAccessConsent = getGdprPurposeConsent(deviceConsentIndex);

        // deviceAccess undefined and gdprApplies undefined
        if (deviceAccessConsent == null && gdprApplies == null) {
            return true;
        }

        // deviceAccess undefined and gdprApplies false
        if (deviceAccessConsent == null && Boolean.FALSE.equals(gdprApplies)) {
            return true;
        }

        // deviceAccess true
        return Boolean.TRUE.equals(deviceAccessConsent);
    }

    /**
     * @return true if {@link #gdpr2CmpSdkId} is grater or equal than 0, false otherwise.
     */
    @VisibleForTesting
    boolean shouldUseTcfV2() {
        return gdpr2CmpSdkId >= 0;
    }

    @VisibleForTesting
    String getSubjectToGdprTcf2() {
        if (gdpr2Subject == NOT_ASSIGNED) {
            return null;
        }

        return String.valueOf(gdpr2Subject);
    }

}
