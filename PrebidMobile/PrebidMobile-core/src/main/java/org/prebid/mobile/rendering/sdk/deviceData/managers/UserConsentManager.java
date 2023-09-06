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
import android.util.Log;

import androidx.annotation.Nullable;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.sdk.BaseManager;

/**
 * UserConsent manager. It is initialized during SDK initialization.
 * It uses Prebid values (for Prebid SDK only) and "real" values
 * according to the standards.
 */
public class UserConsentManager extends BaseManager {

    /* TCF v2 */
    public static final String GDPR_2_SUBJECT_KEY = "IABTCF_gdprApplies";
    public static final String GDPR_2_CONSENT_KEY = "IABTCF_TCString";
    public static final String GDPR_2_PURPOSE_CONSENT_KEY = "IABTCF_PurposeConsents";

    @Nullable
    private static Boolean prebidGdpr2Subject;
    @Nullable
    private static String prebidGdpr2Consent;
    @Nullable
    private static String prebidGdpr2PurposeConsents;

    private int realGdpr2Subject = NOT_ASSIGNED;
    @Nullable
    private String realGdpr2Consent;
    @Nullable
    private String realGdpr2PurposeConsents;

    /* CCPA */
    public static final String US_PRIVACY_KEY = "IABUSPrivacy_String";
    @Nullable
    private static String prebidUsPrivacyString;
    @Nullable
    private String realUsPrivacyString;

    /* COPPA */
    @Nullable
    private static Boolean prebidCoppaSubject;

    /* GPP */
    public static final String GPP_STRING_KEY = "IABGPP_HDR_GppString";
    public static final String GPP_SID_KEY = "IABGPP_GppSID";
    @Nullable
    private String realGppString;
    @Nullable
    private String realGppSid;


    /* Other */
    static final int NOT_ASSIGNED = -1;

    private static final String[] GDPR_CONSENTS = new String[]{
        GDPR_2_SUBJECT_KEY,
        GDPR_2_CONSENT_KEY,
        GDPR_2_PURPOSE_CONSENT_KEY,
        US_PRIVACY_KEY,
        GPP_STRING_KEY,
        GPP_SID_KEY,
    };

    private final SharedPreferences sharedPreferences;

    /**
     * We should keep strong reference to this listener.
     *
     * @see SharedPreferences#registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener)
     */
    @SuppressWarnings("FieldCanBeLocal")
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesListener;

    public UserConsentManager(Context context) {
        super(context);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        preferencesListener = this::updateConsentValue;
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferencesListener);
    }

    public void initConsentValues() {
        for (String consent : GDPR_CONSENTS) {
            updateConsentValue(sharedPreferences, consent);
        }
    }

    /**
     * Automatically updates consents values in the manager when they are changed.
     */
    private void updateConsentValue(
        SharedPreferences preferences,
        @Nullable String key
    ) {
        if (key == null) return;
        try {
            switch (key) {
                case GDPR_2_SUBJECT_KEY:
                    realGdpr2Subject = preferences.getInt(GDPR_2_SUBJECT_KEY, NOT_ASSIGNED);
                    break;
                case GDPR_2_CONSENT_KEY:
                    realGdpr2Consent = preferences.getString(GDPR_2_CONSENT_KEY, null);
                    break;
                case US_PRIVACY_KEY:
                    realUsPrivacyString = preferences.getString(US_PRIVACY_KEY, null);
                    break;
                case GDPR_2_PURPOSE_CONSENT_KEY:
                    realGdpr2PurposeConsents = preferences.getString(GDPR_2_PURPOSE_CONSENT_KEY, null);
                    break;
                case GPP_STRING_KEY:
                    realGppString = preferences.getString(GPP_STRING_KEY, null);
                    break;
                case GPP_SID_KEY:
                    realGppSid = preferences.getString(GPP_SID_KEY, null);
                    break;
            }
        } catch (Exception e) {
            LogUtil.error(String.format("Failed to update %s %s", key, Log.getStackTraceString(e)));
        }
    }


    @Nullable
    public Boolean getSubjectToCoppa() {
        return prebidCoppaSubject;
    }

    public void setSubjectToCoppa(@Nullable Boolean value) {
        prebidCoppaSubject = value;
    }

    @Nullable
    public Boolean getSubjectToGdpr() {
        if (prebidGdpr2Subject != null) {
            return prebidGdpr2Subject;
        }

        return getRealSubjectToGdprBoolean();
    }

    @Nullable
    protected Boolean getRealSubjectToGdprBoolean() {
        if (realGdpr2Subject == 0) {
            return false;
        } else if (realGdpr2Subject == 1) {
            return true;
        }
        return null;
    }

    public void setSubjectToGdpr(@Nullable Boolean value) {
        prebidGdpr2Subject = value;
    }

    @Nullable
    public String getGdprConsent() {
        if (prebidGdpr2Consent != null) {
            return prebidGdpr2Consent;
        }

        return realGdpr2Consent;
    }

    public void setGdprConsent(@Nullable String consent) {
        prebidGdpr2Consent = consent;
    }

    @Nullable
    public String getGdprPurposeConsents() {
        if (prebidGdpr2PurposeConsents != null) {
            return prebidGdpr2PurposeConsents;
        }

        return realGdpr2PurposeConsents;
    }

    @Nullable
    public Boolean getGdprPurposeConsent(int index) {
        return getGdprPurposeConsent(getGdprPurposeConsents(), index);
    }

    @Nullable
    private Boolean getGdprPurposeConsent(
        @Nullable String consents,
        int index
    ) {
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
        prebidGdpr2PurposeConsents = consent;
    }

    @Nullable
    public String getUsPrivacyString() {
        if (prebidUsPrivacyString != null) {
            return prebidUsPrivacyString;
        }

        return realUsPrivacyString;
    }

    public void setUsPrivacyString(@Nullable String value) {
        prebidUsPrivacyString = value;
    }

    @Nullable
    public String getRealGppString() {
        return realGppString;
    }

    @Nullable
    public String getRealGppSid() {
        return realGppSid;
    }


    /**
     * Truth table. Fetches advertising identifier based TCF 2.0 Purpose1 value.
     * <p>
     * deviceAccessConsent=true   deviceAccessConsent=false  deviceAccessConsent undefined
     * <p>
     * gdprApplies=false        Yes, read IDFA             No, don’t read IDFA           Yes, read IDFA
     * gdprApplies=true         Yes, read IDFA             No, don’t read IDFA           No, don’t read IDFA
     * gdprApplies=undefined    Yes, read IDFA             No, don’t read IDFA           Yes, read IDFA
     */
    public boolean canAccessDeviceData() {
        final int deviceConsentIndex = 0;

        if (prebidGdpr2Subject != null && prebidGdpr2PurposeConsents != null && prebidGdpr2PurposeConsents.length() > 0) {
            return checkDeviceDataAccess(
                prebidGdpr2Subject.equals(Boolean.TRUE),
                prebidGdpr2PurposeConsents.charAt(deviceConsentIndex) == '1'
            );
        }

        Boolean gdprApplies = getRealSubjectToGdprBoolean();
        Boolean deviceAccessConsent = getGdprPurposeConsent(realGdpr2PurposeConsents, deviceConsentIndex);
        return checkDeviceDataAccess(gdprApplies, deviceAccessConsent);
    }

    private boolean checkDeviceDataAccess(
        @Nullable Boolean gdprApplies,
        @Nullable Boolean deviceAccessConsent
    ) {
        if (deviceAccessConsent == null && gdprApplies == null) {
            return true;
        }

        if (deviceAccessConsent == null && Boolean.FALSE.equals(gdprApplies)) {
            return true;
        }

        return Boolean.TRUE.equals(deviceAccessConsent);
    }

}
